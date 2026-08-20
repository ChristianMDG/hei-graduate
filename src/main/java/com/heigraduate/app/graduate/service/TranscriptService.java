package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.contract.FinalGradeQuery;
import com.heigraduate.app.graduate.dto.AnnualAverageResult;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.StudentRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TranscriptService {

  private static final float MARGIN = 50f;
  private static final float LINE_HEIGHT = 18f;
  private static final String BUCKET_KEY_PREFIX = "releves/";
  private static final java.time.Duration PRESIGN_VALIDITY = java.time.Duration.ofDays(7);

  private final StudentRepository studentRepository;
  private final CourseRepository courseRepository;
  private final AcademicYearRepository academicYearRepository;
  private final AcademicAverageService academicAverageService;
  private final FinalGradeQuery finalGradeQuery;
  private final com.heigraduate.app.graduate.repository.TranscriptRepository transcriptRepository;
  private final com.heigraduate.app.file.bucket.BucketComponent bucketComponent;

  @Transactional
  public byte[] generateTranscript(UUID studentId, UUID academicYearId) {
    Student student =
        studentRepository
            .findById(studentId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Student not found with id: " + studentId));

    AcademicYear academicYear =
        academicYearRepository
            .findById(academicYearId)
            .orElseThrow(
                () -> new ResourceNotFoundException("AcademicYear not found: " + academicYearId));

    AnnualAverageResult summary =
        academicAverageService.computeAnnualAverage(studentId, academicYearId);

    List<TranscriptLine> lines = buildLines(studentId, summary);
    boolean isComplete = summary.missingGradeCourseIds().isEmpty();

    byte[] pdf = renderPdf(student, academicYear, summary, lines, isComplete);

    String bucketKey =
        BUCKET_KEY_PREFIX
            + student.getStudentNumber()
            + "-"
            + academicYear.getLabel()
            + "-"
            + java.time.Instant.now().toEpochMilli()
            + ".pdf";

    java.io.File tempFile = null;
    try {
      tempFile = java.io.File.createTempFile("releve-", ".pdf");
      try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
        fos.write(pdf);
      }
      bucketComponent.upload(tempFile, bucketKey);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to upload transcript PDF to S3", e);
    } finally {
      if (tempFile != null) {
        tempFile.delete();
      }
    }

    com.heigraduate.app.graduate.model.Transcript transcript =
        com.heigraduate.app.graduate.model.Transcript.builder()
            .student(student)
            .academicYear(academicYear)
            .semester(null)
            .type(isComplete ? "COMPLET" : "PROVISOIRE")
            .urlS3(bucketKey)
            .averageGrade(summary.average())
            .obtainedCredits(summary.obtainedCredits())
            .expectedCredits(summary.expectedCredits())
            .yearValidated(summary.isYearValidated())
            .emailSent(false)
            .build();
    transcriptRepository.save(transcript);

    return pdf;
  }

  @Transactional(readOnly = true)
  public byte[] generateTranscriptForUser(UUID userId) {
    return generateTranscriptForUser(userId, null);
  }

  @Transactional(readOnly = true)
  public byte[] generateTranscriptForUser(UUID userId, UUID academicYearId) {
    Student student =
        studentRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("No student profile linked to user: " + userId));

    UUID targetYearId = academicYearId != null ? academicYearId : resolveCurrentAcademicYearId();
    return generateTranscript(student.getId(), targetYearId);
  }

  private List<TranscriptLine> buildLines(UUID studentId, AnnualAverageResult summary) {
    List<UUID> allCourseIds = new ArrayList<>();
    allCourseIds.addAll(summary.validatedCourseIds());
    allCourseIds.addAll(summary.notValidatedCourseIds());
    allCourseIds.addAll(summary.missingGradeCourseIds());

    Map<UUID, Course> coursesById = new LinkedHashMap<>();
    for (Course course : courseRepository.findAllById(allCourseIds)) {
      coursesById.put(course.getId(), course);
    }

    List<TranscriptLine> lines = new ArrayList<>();
    for (UUID courseId : allCourseIds) {
      Course course = coursesById.get(courseId);
      if (course == null) {
        throw new ResourceNotFoundException("Course not found with id: " + courseId);
      }
      Optional<BigDecimal> finalGrade = finalGradeQuery.getFinalGrade(studentId, courseId);
      lines.add(
          new TranscriptLine(
              course.getCourseReference(), course.getTitle(), course.getCredits(), finalGrade));
    }
    return lines;
  }

  private UUID resolveCurrentAcademicYearId() {
    LocalDate today = LocalDate.now();
    return academicYearRepository.findAll().stream()
        .filter(y -> !today.isBefore(y.getStartDate()) && !today.isAfter(y.getEndDate()))
        .findFirst()
        .map(AcademicYear::getId)
        .orElseGet(
            () ->
                academicYearRepository.findAll().stream()
                    .sorted((a, b) -> b.getEndDate().compareTo(a.getEndDate()))
                    .findFirst()
                    .map(AcademicYear::getId)
                    .orElseThrow(
                        () ->
                            new ResourceNotFoundException(
                                "No active academic year found for date: " + today)));
  }

  private byte[] renderPdf(
      Student student,
      AcademicYear academicYear,
      AnnualAverageResult summary,
      List<TranscriptLine> lines,
      boolean isComplete) {
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);

      try (PDPageContentStream content = new PDPageContentStream(document, page)) {
        float y = page.getMediaBox().getHeight() - MARGIN;

        y =
            writeLine(
                content,
                y,
                PDType1Font.HELVETICA_BOLD,
                16,
                isComplete ? "RELEVÉ DE NOTES" : "RELEVÉ DE NOTES PROVISOIRE");
        y -= LINE_HEIGHT;

        y =
            writeLine(
                content,
                y,
                PDType1Font.HELVETICA,
                11,
                student.getLastName()
                    + " "
                    + student.getFirstName()
                    + " ("
                    + student.getStudentNumber()
                    + ")");
        y =
            writeLine(
                content,
                y,
                PDType1Font.HELVETICA,
                11,
                "Année universitaire : " + academicYear.getLabel());
        y -= LINE_HEIGHT;

        for (TranscriptLine line : lines) {
          String gradeText = line.finalGrade().map(g -> g.toString() + "/20").orElse("En attente");
          y =
              writeLine(
                  content,
                  y,
                  PDType1Font.HELVETICA,
                  10,
                  line.courseReference()
                      + " - "
                      + line.courseTitle()
                      + " ("
                      + line.credits()
                      + " crédits) : "
                      + gradeText);
        }
        y -= LINE_HEIGHT;

        String averageText = summary.average() != null ? summary.average() + "/20" : "En attente";
        y =
            writeLine(
                content,
                y,
                PDType1Font.HELVETICA_BOLD,
                11,
                "Moyenne générale annuelle : " + averageText);
        y =
            writeLine(
                content,
                y,
                PDType1Font.HELVETICA,
                11,
                "Crédits obtenus : "
                    + summary.obtainedCredits()
                    + " / "
                    + summary.expectedCredits());
        writeLine(
            content,
            y,
            PDType1Font.HELVETICA_BOLD,
            11,
            "Année validée : " + (summary.isYearValidated() ? "Oui" : "Non"));
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      document.save(out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to generate transcript PDF", e);
    }
  }

  private float writeLine(
      PDPageContentStream content, float y, PDType1Font font, float fontSize, String text)
      throws IOException {
    content.beginText();
    content.setFont(font, fontSize);
    content.newLineAtOffset(MARGIN, y);
    content.showText(text);
    content.endText();
    return y - LINE_HEIGHT;
  }

  private record TranscriptLine(
      String courseReference, String courseTitle, int credits, Optional<BigDecimal> finalGrade) {}
}
