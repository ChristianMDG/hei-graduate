package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.heigraduate.app.file.bucket.BucketComponent;
import com.heigraduate.app.file.hash.FileHash;
import com.heigraduate.app.file.hash.FileHashAlgorithm;
import com.heigraduate.app.graduate.dto.DiplomaExcelResponse;
import com.heigraduate.app.graduate.dto.DiplomaResponse;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Parcours;
import com.heigraduate.app.graduate.model.Promotion;
import com.heigraduate.app.graduate.repository.DiplomaListRepository;
import com.heigraduate.app.graduate.repository.DiplomaRepository;
import com.heigraduate.app.graduate.repository.ParcoursRepository;
import com.heigraduate.app.graduate.repository.PromotionRepository;
import com.heigraduate.app.graduate.service.DiplomaExcelService;
import com.heigraduate.app.graduate.service.RankingService;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiplomaExcelServiceTest {

  @Mock private PromotionRepository promotionRepository;
  @Mock private ParcoursRepository parcoursRepository;
  @Mock private DiplomaRepository diplomaRepository;

  @Mock private DiplomaListRepository diplomaListRepository;
  @Mock private RankingService rankingService;
  @Mock private BucketComponent bucketComponent;

  @InjectMocks private DiplomaExcelService diplomaExcelService;

  private UUID promotionId;
  private Promotion promotion;

  @BeforeEach
  void setUp() {
    promotionId = UUID.randomUUID();
    AcademicYear finalYear =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 6, 30))
            .level("L3")
            .build();
    promotion =
        Promotion.builder()
            .id(promotionId)
            .label("Promo 2026")
            .finalAcademicYear(finalYear)
            .build();
  }

  private DiplomaResponse diplomaResponse(
      UUID parcoursId,
      int rank,
      String studentNumber,
      String lastName,
      String firstName,
      String average) {
    return new DiplomaResponse(
        UUID.randomUUID(),
        UUID.randomUUID(),
        studentNumber,
        lastName,
        firstName,
        promotionId,
        parcoursId,
        LocalDate.now(),
        new BigDecimal(average),
        rank,
        "Bien");
  }

  @Test
  void generateExcel_shouldCreateOneSheetPerParcours_withExpectedColumns() throws Exception {
    UUID elId = UUID.randomUUID();
    UUID tnId = UUID.randomUUID();
    Parcours el = Parcours.builder().id(elId).code("EL").label("Electronique").active(true).build();
    Parcours tn = Parcours.builder().id(tnId).code("TN").label("Telecom").active(true).build();

    when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));
    when(diplomaRepository.findDistinctParcoursIdsByPromotionId(promotionId))
        .thenReturn(List.of(elId, tnId));
    when(parcoursRepository.findById(elId)).thenReturn(Optional.of(el));
    when(parcoursRepository.findById(tnId)).thenReturn(Optional.of(tn));
    when(rankingService.getRanking(promotionId, elId))
        .thenReturn(List.of(diplomaResponse(elId, 1, "STD001", "Rakoto", "Jean", "16.42")));
    when(rankingService.getRanking(promotionId, tnId))
        .thenReturn(List.of(diplomaResponse(tnId, 1, "STD002", "Rasoa", "Marie", "15.10")));

    when(bucketComponent.upload(any(File.class), anyString()))
        .thenReturn(new FileHash(FileHashAlgorithm.SHA256, "checksum"));
    when(bucketComponent.presign(anyString(), any()))
        .thenReturn(new URL("https://bucket.s3.amazonaws.com/diplomas/generated.xlsx"));

    when(diplomaListRepository.findByPromotionIdAndParcoursId(any(), any()))
        .thenReturn(java.util.Optional.empty());
    when(diplomaListRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    DiplomaExcelResponse response = diplomaExcelService.generateExcel(promotionId);

    assertThat(response.downloadUrl())
        .isEqualTo("https://bucket.s3.amazonaws.com/diplomas/generated.xlsx");

    ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
    verify(bucketComponent).upload(fileCaptor.capture(), anyString());

    try (XSSFWorkbook workbook = new XSSFWorkbook(fileCaptor.getValue())) {
      assertThat(workbook.getNumberOfSheets()).isEqualTo(2);

      Sheet elSheet = workbook.getSheet("EL");
      assertThat(elSheet).isNotNull();
      Row elHeader = elSheet.getRow(0);
      assertThat(elHeader.getCell(0).getStringCellValue()).isEqualTo("Promotion");
      assertThat(elHeader.getCell(1).getStringCellValue()).isEqualTo("Parcours");
      assertThat(elHeader.getCell(2).getStringCellValue()).isEqualTo("Rang");
      assertThat(elHeader.getCell(3).getStringCellValue()).isEqualTo("N° Étudiant");
      assertThat(elHeader.getCell(4).getStringCellValue()).isEqualTo("Nom");
      assertThat(elHeader.getCell(5).getStringCellValue()).isEqualTo("Prénom");
      assertThat(elHeader.getCell(6).getStringCellValue()).isEqualTo("Moyenne générale");

      Row elDataRow = elSheet.getRow(1);
      assertThat(elDataRow.getCell(0).getStringCellValue()).isEqualTo("Promo 2026");
      assertThat(elDataRow.getCell(1).getStringCellValue()).isEqualTo("Electronique");
      assertThat(elDataRow.getCell(2).getNumericCellValue()).isEqualTo(1.0);
      assertThat(elDataRow.getCell(3).getStringCellValue()).isEqualTo("STD001");
      assertThat(elDataRow.getCell(4).getStringCellValue()).isEqualTo("Rakoto");
      assertThat(elDataRow.getCell(5).getStringCellValue()).isEqualTo("Jean");
      assertThat(elDataRow.getCell(6).getNumericCellValue()).isEqualTo(16.42);

      Sheet tnSheet = workbook.getSheet("TN");
      assertThat(tnSheet).isNotNull();
      assertThat(tnSheet.getRow(1).getCell(3).getStringCellValue()).isEqualTo("STD002");
    }
  }

  @Test
  void generateExcel_shouldThrow_whenPromotionNotFound() {
    when(promotionRepository.findById(promotionId)).thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> diplomaExcelService.generateExcel(promotionId));

    verifyNoInteractions(bucketComponent);
  }

  @Test
  void generateExcel_shouldThrow_whenNoDiplomasGeneratedYet() {
    when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));
    when(diplomaRepository.findDistinctParcoursIdsByPromotionId(promotionId)).thenReturn(List.of());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> diplomaExcelService.generateExcel(promotionId));

    verifyNoInteractions(bucketComponent);
  }

  @Test
  void getExistingExcel_shouldReturnPresignedUrl_whenExcelExists() throws Exception {
    com.heigraduate.app.graduate.model.DiplomaList diplomaList =
        com.heigraduate.app.graduate.model.DiplomaList.builder()
            .promotionId(promotionId)
            .parcoursId(UUID.randomUUID())
            .urlS3("diplomas/existing.xlsx")
            .build();

    when(diplomaListRepository.findByPromotionId(promotionId)).thenReturn(List.of(diplomaList));
    when(bucketComponent.presign(eq("diplomas/existing.xlsx"), any()))
        .thenReturn(new URL("https://bucket.s3.amazonaws.com/diplomas/existing.xlsx"));

    DiplomaExcelResponse response = diplomaExcelService.getExistingExcel(promotionId);

    assertThat(response.downloadUrl())
        .isEqualTo("https://bucket.s3.amazonaws.com/diplomas/existing.xlsx");
  }

  @Test
  void getExistingExcel_shouldThrow_whenNoExcelExistsYet() {
    when(diplomaListRepository.findByPromotionId(promotionId)).thenReturn(List.of());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> diplomaExcelService.getExistingExcel(promotionId));
  }
}
