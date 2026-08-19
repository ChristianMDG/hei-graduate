package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.contract.FinalGradeQuery;
import com.heigraduate.app.graduate.dto.GradeHistoryResponse;
import com.heigraduate.app.graduate.dto.GradeRequest;
import com.heigraduate.app.graduate.dto.GradeResponse;
import com.heigraduate.app.graduate.dto.GradeUpdateRequest;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.mapper.GradeHistoryMapper;
import com.heigraduate.app.graduate.mapper.GradeMapper;
import com.heigraduate.app.graduate.model.Exam;
import com.heigraduate.app.graduate.model.Grade;
import com.heigraduate.app.graduate.model.GradeHistory;
import com.heigraduate.app.graduate.model.GradeStatus;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.model.Teacher;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.repository.ExamRepository;
import com.heigraduate.app.graduate.repository.GradeHistoryRepository;
import com.heigraduate.app.graduate.repository.GradeRepository;
import com.heigraduate.app.graduate.repository.StudentRepository;
import com.heigraduate.app.graduate.repository.TeacherRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeService implements FinalGradeQuery {

  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;
  private final StudentRepository studentRepository;
  private final ExamRepository examRepository;
  private final TeacherRepository teacherRepository;
  private final AssignmentService assignmentService;
  private final com.heigraduate.app.graduate.validator.ExamValidator examValidator;

  @Transactional(readOnly = true)
  public List<GradeResponse> findAll() {
    return gradeRepository.findAll().stream().map(GradeMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public GradeResponse findById(UUID id) {
    return gradeRepository
        .findById(id)
        .map(GradeMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));
  }

  @Transactional(readOnly = true)
  public List<GradeResponse> findPublishedForStudent(UUID studentId) {
    return gradeRepository.findByStudentIdAndStatus(studentId, GradeStatus.PUBLISHED).stream()
        .map(GradeMapper::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<GradeResponse> findMyPublishedGrades(UUID userId) {
    Student student =
        studentRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("No student profile linked to user: " + userId));

    return findPublishedForStudent(student.getId());
  }

  @Transactional(readOnly = true)
  public List<GradeResponse> findGradesForStudent(UUID studentId, User actingUser) {
    if ("ADMIN".equals(actingUser.getRole().name())) {
      return gradeRepository.findByStudentId(studentId).stream()
          .map(GradeMapper::toResponse)
          .toList();
    }

    Teacher teacher =
        teacherRepository
            .findByUserId(actingUser.getId())
            .orElseThrow(
                () -> new AccessDeniedException("No teacher profile linked to this account"));

    return gradeRepository.findByStudentId(studentId).stream()
        .filter(
            grade ->
                assignmentService.isTeacherAssignedToCourse(
                    teacher.getId(),
                    grade.getExam().getCourse().getId(),
                    grade.getExam().getAcademicYear().getId()))
        .map(GradeMapper::toResponse)
        .toList();
  }

  @Transactional
  public GradeResponse create(GradeRequest request, User actingUser) {
    if (gradeRepository.existsByStudentIdAndExamId(request.studentId(), request.examId())) {
      throw new ConflictException("A grade already exists for this student and this exam");
    }

    Student student =
        studentRepository
            .findById(request.studentId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Student not found with id: " + request.studentId()));

    Exam exam =
        examRepository
            .findById(request.examId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Exam not found with id: " + request.examId()));

    validateTeacherCourseAssignment(actingUser, exam);

    Grade grade =
        Grade.builder()
            .student(student)
            .exam(exam)
            .value(request.value())
            .enteredByUserId(actingUser.getId())
            .status(GradeStatus.DRAFT)
            .build();

    return GradeMapper.toResponse(gradeRepository.save(grade));
  }

  @Transactional
  public GradeResponse update(UUID id, GradeUpdateRequest request, User actingUser) {
    Grade grade =
        gradeRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

    validateTeacherCourseAssignment(actingUser, grade.getExam());

    BigDecimal oldValue = grade.getValue();

    GradeHistory history =
        GradeHistory.builder()
            .grade(grade)
            .oldValue(oldValue)
            .newValue(request.value())
            .reason(request.reason())
            .changedByUserId(actingUser.getId())
            .build();

    gradeHistoryRepository.save(history);

    grade.setValue(request.value());

    return GradeMapper.toResponse(gradeRepository.save(grade));
  }

  private void validateTeacherCourseAssignment(User actingUser, Exam exam) {
    if ("ADMIN".equals(actingUser.getRole().name())) {
      return;
    }

    if ("TEACHER".equals(actingUser.getRole().name())) {

      Teacher teacher =
          teacherRepository
              .findByUserId(actingUser.getId())
              .orElseThrow(
                  () -> new AccessDeniedException("No teacher profile linked to this account"));

      boolean assigned =
          assignmentService.isTeacherAssignedToCourse(
              teacher.getId(), exam.getCourse().getId(), exam.getAcademicYear().getId());

      if (!assigned) {
        throw new AccessDeniedException(
            "Teacher is not assigned to this course for this academic year");
      }
    }
  }

  @Transactional(readOnly = true)
  public List<GradeHistoryResponse> getHistory(UUID gradeId) {
    if (!gradeRepository.existsById(gradeId)) {
      throw new ResourceNotFoundException("Grade not found with id: " + gradeId);
    }

    return gradeHistoryRepository.findByGradeId(gradeId).stream()
        .map(GradeHistoryMapper::toResponse)
        .toList();
  }

  @Transactional
  public GradeResponse publish(UUID id) {
    Grade grade =
        gradeRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

    Exam exam = grade.getExam();

    if (exam.getSemester() != null) {
      examValidator.validateCoefficientSumEqualsOne(
          exam.getCourse().getId(), exam.getSemester().getId());
    }

    grade.setStatus(GradeStatus.PUBLISHED);

    return GradeMapper.toResponse(gradeRepository.save(grade));
  }

  @Transactional
  public void delete(UUID id) {
    if (!gradeRepository.existsById(id)) {
      throw new ResourceNotFoundException("Grade not found with id: " + id);
    }

    gradeRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<BigDecimal> getFinalGrade(UUID studentId, UUID courseId) {
    List<Grade> publishedGrades =
        gradeRepository.findPublishedByStudentIdAndCourseId(studentId, courseId);

    if (publishedGrades.isEmpty()) {
      return Optional.empty();
    }

    BigDecimal weightedSum = BigDecimal.ZERO;
    BigDecimal totalWeight = BigDecimal.ZERO;

    for (Grade g : publishedGrades) {
      BigDecimal weight =
          BigDecimal.valueOf(g.getExam().getCoefficientNumerator())
              .divide(
                  BigDecimal.valueOf(g.getExam().getCoefficientDenominator()),
                  6,
                  RoundingMode.HALF_UP);

      weightedSum = weightedSum.add(g.getValue().multiply(weight));
      totalWeight = totalWeight.add(weight);
    }

    if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
      return Optional.empty();
    }

    return Optional.of(weightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP));
  }
}
