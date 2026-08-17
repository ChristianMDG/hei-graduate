package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.contract.CourseRequirementQuery;
import com.heigraduate.app.graduate.contract.FinalGradeQuery;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.StudentRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

  private final StudentRepository studentRepository;
  private final CourseRepository courseRepository;
  private final AcademicYearRepository academicYearRepository;
  private final EnrollmentService enrollmentService;
  private final CourseRequirementQuery courseRequirementQuery;
  private final FinalGradeQuery finalGradeQuery;

  @Transactional(readOnly = true)
  public byte[] generateTranscript(UUID studentId, UUID trackId, UUID academicYearId) {
    Student student =
        studentRepository
            .findById(studentId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Student not found with id: " + studentId));

    List<UUID> mandatoryCourseIds =
        courseRequirementQuery.getMandatoryCourseIds(trackId, academicYearId);

    List<TranscriptLine> lines =
        mandatoryCourseIds.stream()
            .map(
                courseId -> {
                  Course course =
                      courseRepository
                          .findById(courseId)
                          .orElseThrow(
                              () ->
                                  new ResourceNotFoundException(
                                      "Course not found with id: " + courseId));
                  Optional<BigDecimal> finalGrade =
                      finalGradeQuery.getFinalGrade(studentId, courseId);
                  return new TranscriptLine(
                      course.getCourseReference(), course.getTitle(), finalGrade);
                })
            .toList();

    boolean isComplete = lines.stream().allMatch(line -> line.finalGrade().isPresent());

    return renderPdf(student, lines, isComplete);
  }

  @Transactional(readOnly = true)
  public byte[] generateTranscriptForUser(UUID userId) {
    Student student =
        studentRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("No student profile linked to user: " + userId));

    Enrollment currentEnrollment = enrollmentService.getCurrent(student.getId());
    UUID academicYearId = resolveCurrentAcademicYearId();

    return generateTranscript(student.getId(), currentEnrollment.getParcoursId(), academicYearId);
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

  private byte[] renderPdf(Student student, List<TranscriptLine> lines, boolean isComplete) {
    try (PDDocument document = new PDDocument()) {
      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);

      try (PDPageContentStream content = new PDPageContentStream(document, page)) {
        float y = page.getMediaBox().getHeight() - MARGIN;

        content.beginText();
        content.setFont(PDType1Font.HELVETICA_BOLD, 16);
        content.newLineAtOffset(MARGIN, y);
        content.showText(isComplete ? "Relevé de notes" : "Relevé de notes provisoires");
        content.endText();
        y -= LINE_HEIGHT * 2;

        content.beginText();
        content.setFont(PDType1Font.HELVETICA, 11);
        content.newLineAtOffset(MARGIN, y);
        content.showText(
            student.getLastName()
                + " "
                + student.getFirstName()
                + " ("
                + student.getStudentNumber()
                + ")");
        content.endText();
        y -= LINE_HEIGHT * 2;

        content.setFont(PDType1Font.HELVETICA, 10);
        for (TranscriptLine line : lines) {
          content.beginText();
          content.newLineAtOffset(MARGIN, y);
          String gradeText = line.finalGrade().map(g -> g.toString() + "/20").orElse("En attente");
          content.showText(line.courseReference() + " - " + line.courseTitle() + " : " + gradeText);
          content.endText();
          y -= LINE_HEIGHT;
        }
      }

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      document.save(out);
      return out.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to generate transcript PDF", e);
    }
  }

  private record TranscriptLine(
      String courseReference, String courseTitle, Optional<BigDecimal> finalGrade) {}
}
