package com.heigraduate.app.graduate.service;

import com.heigraduate.app.file.bucket.BucketComponent;
import com.heigraduate.app.graduate.dto.DiplomaExcelResponse;
import com.heigraduate.app.graduate.dto.DiplomaResponse;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.DiplomaList;
import com.heigraduate.app.graduate.model.Parcours;
import com.heigraduate.app.graduate.model.Promotion;
import com.heigraduate.app.graduate.repository.DiplomaListRepository;
import com.heigraduate.app.graduate.repository.DiplomaRepository;
import com.heigraduate.app.graduate.repository.ParcoursRepository;
import com.heigraduate.app.graduate.repository.PromotionRepository;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiplomaExcelService {

  private static final String BUCKET_KEY_PREFIX = "diplomas/";
  private static final Duration DOWNLOAD_LINK_VALIDITY = Duration.ofMinutes(15);
  private static final List<String> HEADERS =
      List.of("Promotion", "Parcours", "Rang", "N° Étudiant", "Nom", "Prénom", "Moyenne générale");

  private final PromotionRepository promotionRepository;
  private final ParcoursRepository parcoursRepository;
  private final DiplomaRepository diplomaRepository;
  private final DiplomaListRepository diplomaListRepository;
  private final RankingService rankingService;
  private final BucketComponent bucketComponent;

le le re-téléchargement sans recalculer tout le classement.

  @Transactional
  public DiplomaExcelResponse generateExcel(UUID promotionId) {
    Promotion promotion =
        promotionRepository
            .findById(promotionId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Promotion not found: " + promotionId));

    List<UUID> parcoursIds = diplomaRepository.findDistinctParcoursIdsByPromotionId(promotionId);
    if (parcoursIds.isEmpty()) {
      throw new ResourceNotFoundException(
          "No diplomas have been generated yet for promotion: " + promotionId);
    }

    File workbookFile = writeWorkbook(promotion, parcoursIds);
    String bucketKey = BUCKET_KEY_PREFIX + promotionId + "/" + UUID.randomUUID() + ".xlsx";
    bucketComponent.upload(workbookFile, bucketKey);

    for (UUID parcoursId : parcoursIds) {
      diplomaListRepository
          .findByPromotionIdAndParcoursId(promotionId, parcoursId)
          .ifPresentOrElse(
              existing -> existing.setUrlS3(bucketKey),
              () ->
                  diplomaListRepository.save(
                      DiplomaList.builder()
                          .promotionId(promotionId)
                          .parcoursId(parcoursId)
                          .urlS3(bucketKey)
                          .build()));
    }

    var downloadUrl = bucketComponent.presign(bucketKey, DOWNLOAD_LINK_VALIDITY);
    return new DiplomaExcelResponse(downloadUrl.toString());
  }

  @Transactional(readOnly = true)
  public DiplomaExcelResponse getExistingExcel(UUID promotionId) {
    List<DiplomaList> lists = diplomaListRepository.findByPromotionId(promotionId);
    if (lists.isEmpty() || lists.get(0).getUrlS3() == null) {
      throw new ResourceNotFoundException(
          "No Excel file has been generated yet for promotion: " + promotionId);
    }
    String bucketKey = lists.get(0).getUrlS3();
    var downloadUrl = bucketComponent.presign(bucketKey, DOWNLOAD_LINK_VALIDITY);
    return new DiplomaExcelResponse(downloadUrl.toString());
  }

  private File writeWorkbook(Promotion promotion, List<UUID> parcoursIds) {
    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
      for (UUID parcoursId : parcoursIds) {
        Parcours parcours =
            parcoursRepository
                .findById(parcoursId)
                .orElseThrow(
                    () -> new ResourceNotFoundException("Parcours not found: " + parcoursId));
        List<DiplomaResponse> ranking = rankingService.getRanking(promotion.getId(), parcoursId);
        writeSheet(workbook, promotion, parcours, ranking);
      }

      File tempFile = Files.createTempFile("diplomas-" + promotion.getId(), ".xlsx").toFile();
      try (FileOutputStream out = new FileOutputStream(tempFile)) {
        workbook.write(out);
      }
      return tempFile;
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to generate the graduates Excel file", e);
    }
  }

  private void writeSheet(
      XSSFWorkbook workbook,
      Promotion promotion,
      Parcours parcours,
      List<DiplomaResponse> ranking) {
    Sheet sheet = workbook.createSheet(sheetName(parcours));

    Row headerRow = sheet.createRow(0);
    for (int col = 0; col < HEADERS.size(); col++) {
      headerRow.createCell(col, CellType.STRING).setCellValue(HEADERS.get(col));
    }

    int rowIndex = 1;
    for (DiplomaResponse diploma : ranking) {
      Row row = sheet.createRow(rowIndex++);
      row.createCell(0, CellType.STRING).setCellValue(promotion.getLabel());
      row.createCell(1, CellType.STRING).setCellValue(parcours.getLabel());
      row.createCell(2, CellType.NUMERIC).setCellValue(diploma.rank().doubleValue());
      row.createCell(3, CellType.STRING).setCellValue(diploma.studentNumber());
      row.createCell(4, CellType.STRING).setCellValue(diploma.lastName());
      row.createCell(5, CellType.STRING).setCellValue(diploma.firstName());
      row.createCell(6, CellType.NUMERIC).setCellValue(diploma.overallAverage().doubleValue());
    }
  }

  private String sheetName(Parcours parcours) {
    return parcours.getCode();
  }
}
