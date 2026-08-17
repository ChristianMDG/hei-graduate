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

/**
 * Generates a student's transcript (RELEVE, §10) as a PDF.
 *
 * <p>Reuses {@link AcademicAverageService} for the annual average, obtained/expected credits and
 * course validation status, instead of recomputing them - §10 and §11 must never disagree on
 * whether a course or a year is validated.
 *
 * <p>Known limitation: the subject also asks for "le semestre ou les semestres concernés", but
 * nothing in the current schema links a Course or CourseTrack to a Semester (Semester only has its
 * own start/end dates, no FK to AcademicYear either) - there is no way to compute this field
 * without a schema change on Membre 1's SEMESTRE domain. Left out rather than guessed.
 */
@Service
@RequiredArgsConstructor
public class TranscriptService {

  private static final float MARGIN = 50f;
  private static final float LINE_HEIGHT = 18f;

  private final StudentRepository studentRepository;
  private final CourseRepository courseRepository;
  private final AcademicYearRepository academicYearRepository;
  private final AcademicAverageService academicAverageService;
  private final FinalGradeQuery finalGradeQuery;

  @Transactional(readOnly = true)
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

    return renderPdf(student, academicYear, summary, lines, isComplete);
  }

  @Transactional(readOnly = true)
  public byte[] generateTranscriptForUser(UUID userId) {
    Student student =
        studentRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("No student profile linked to user: " + userId));

    UUID academicYearId = resolveCurrentAcademicYearId();
    return generateTranscript(student.getId(), academicYearId);
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
        .orElseThrow(
            () ->
                new ResourceNotFoundException("No active academic year found for date: " + today));
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
