package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.contract.FinalGradeQuery;
import com.heigraduate.app.graduate.dto.AnnualAverageResult;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.StudentRepository;
import com.heigraduate.app.graduate.service.AcademicAverageService;
import com.heigraduate.app.graduate.service.TranscriptService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranscriptServiceTest {

  @Mock private StudentRepository studentRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private AcademicAverageService academicAverageService;
  @Mock private FinalGradeQuery finalGradeQuery;

  @InjectMocks private TranscriptService transcriptService;

  private Student student;
  private AcademicYear academicYear;
  private Course course1;
  private Course course2;

  @BeforeEach
  void setUp() {
    student =
        Student.builder()
            .id(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .studentNumber("STD24049")
            .lastName("Lahatra")
            .firstName("Nomena")
            .enrollmentDate(LocalDate.of(2023, 9, 1))
            .status("ACTIVE")
            .build();

    academicYear =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2025-2026")
            .startDate(LocalDate.now().minusMonths(2))
            .endDate(LocalDate.now().plusMonths(6))
            .level("L2")
            .build();

    course1 =
        Course.builder()
            .id(UUID.randomUUID())
            .courseReference("PROG4")
            .title("Programmation 4")
            .credits(5)
            .active(true)
            .build();

    course2 =
        Course.builder()
            .id(UUID.randomUUID())
            .courseReference("ALGO3")
            .title("Algorithmique 3")
            .credits(4)
            .active(true)
            .build();
  }

  private String extractText(byte[] pdf) throws Exception {
    try (PDDocument document = PDDocument.load(pdf)) {
      return new PDFTextStripper().getText(document);
    }
  }

  @Test
  void generateTranscript_shouldMarkComplete_andIncludeAllSection10Fields_whenAllGradesPresent()
      throws Exception {
    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(academicYearRepository.findById(academicYear.getId()))
        .thenReturn(Optional.of(academicYear));
    when(academicAverageService.computeAnnualAverage(student.getId(), academicYear.getId()))
        .thenReturn(
            new AnnualAverageResult(
                student.getId(),
                academicYear.getId(),
                new BigDecimal("13.56"),
                9,
                9,
                List.of(course1.getId(), course2.getId()),
                List.of(),
                List.of()));
    when(courseRepository.findAllById(List.of(course1.getId(), course2.getId())))
        .thenReturn(List.of(course1, course2));
    when(finalGradeQuery.getFinalGrade(student.getId(), course1.getId()))
        .thenReturn(Optional.of(new BigDecimal("14.50")));
    when(finalGradeQuery.getFinalGrade(student.getId(), course2.getId()))
        .thenReturn(Optional.of(new BigDecimal("12.00")));

    byte[] pdf = transcriptService.generateTranscript(student.getId(), academicYear.getId());

    assertThat(pdf).isNotEmpty();
    assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");

    String text = extractText(pdf);
    assertThat(text).contains("RELEVÉ DE NOTES").doesNotContain("PROVISOIRE");
    assertThat(text).contains("STD24049");
    assertThat(text).contains("2025-2026");
    assertThat(text).contains("PROG4").contains("14.50/20");
    assertThat(text).contains("ALGO3").contains("12.00/20");
    assertThat(text).contains("13.56/20");
    assertThat(text).contains("Crédits obtenus : 9 / 9");
    assertThat(text).contains("Année validée : Oui");
  }

  @Test
  void generateTranscript_shouldMarkProvisional_whenAGradeIsMissing() throws Exception {
    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(academicYearRepository.findById(academicYear.getId()))
        .thenReturn(Optional.of(academicYear));
    when(academicAverageService.computeAnnualAverage(student.getId(), academicYear.getId()))
        .thenReturn(
            new AnnualAverageResult(
                student.getId(),
                academicYear.getId(),
                new BigDecimal("14.50"),
                5,
                9,
                List.of(course1.getId()),
                List.of(),
                List.of(course2.getId())));
    when(courseRepository.findAllById(List.of(course1.getId(), course2.getId())))
        .thenReturn(List.of(course1, course2));
    when(finalGradeQuery.getFinalGrade(student.getId(), course1.getId()))
        .thenReturn(Optional.of(new BigDecimal("14.50")));
    when(finalGradeQuery.getFinalGrade(student.getId(), course2.getId()))
        .thenReturn(Optional.empty());

    byte[] pdf = transcriptService.generateTranscript(student.getId(), academicYear.getId());

    String text = extractText(pdf);
    assertThat(text).contains("RELEVÉ DE NOTES PROVISOIRE");
    assertThat(text).contains("En attente");
    assertThat(text).contains("Année validée : Non");
  }

  @Test
  void generateTranscript_shouldThrow_whenStudentNotFound() {
    when(studentRepository.findById(student.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> transcriptService.generateTranscript(student.getId(), academicYear.getId()))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void generateTranscript_shouldThrow_whenAcademicYearNotFound() {
    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(academicYearRepository.findById(academicYear.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> transcriptService.generateTranscript(student.getId(), academicYear.getId()))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void generateTranscriptForUser_shouldResolveStudentAndCurrentYear() {
    when(studentRepository.findByUserId(student.getUserId())).thenReturn(Optional.of(student));
    when(academicYearRepository.findAll()).thenReturn(List.of(academicYear));
    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(academicYearRepository.findById(academicYear.getId()))
        .thenReturn(Optional.of(academicYear));
    when(academicAverageService.computeAnnualAverage(student.getId(), academicYear.getId()))
        .thenReturn(
            new AnnualAverageResult(
                student.getId(),
                academicYear.getId(),
                null,
                0,
                0,
                List.of(),
                List.of(),
                List.of()));

    byte[] pdf = transcriptService.generateTranscriptForUser(student.getUserId());

    assertThat(pdf).isNotEmpty();
  }

  @Test
  void generateTranscriptForUser_shouldThrow_whenNoActiveAcademicYearFound() {
    when(studentRepository.findByUserId(student.getUserId())).thenReturn(Optional.of(student));
    when(academicYearRepository.findAll()).thenReturn(List.of());

    assertThatThrownBy(() -> transcriptService.generateTranscriptForUser(student.getUserId()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("No active academic year");
  }

  @Test
  void generateTranscriptForUser_shouldThrow_whenNoStudentProfileLinkedToUser() {
    UUID orphanUserId = UUID.randomUUID();
    when(studentRepository.findByUserId(orphanUserId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> transcriptService.generateTranscriptForUser(orphanUserId))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
