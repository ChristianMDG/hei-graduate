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
import com.heigraduate.app.graduate.repository.ExamRepository;
import com.heigraduate.app.graduate.repository.GradeHistoryRepository;
import com.heigraduate.app.graduate.repository.GradeRepository;
import com.heigraduate.app.graduate.repository.StudentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeService implements FinalGradeQuery {

  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;
  private final StudentRepository studentRepository;
  private final ExamRepository examRepository;

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

  @Transactional
  public GradeResponse create(GradeRequest request) {
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

    Grade grade =
        Grade.builder()
            .student(student)
            .exam(exam)
            .value(request.value())
            .status(GradeStatus.DRAFT)
            .build();

    return GradeMapper.toResponse(gradeRepository.save(grade));
  }

  @Transactional
  public GradeResponse update(UUID id, GradeUpdateRequest request) {
    Grade grade =
        gradeRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

    BigDecimal oldValue = grade.getValue();
    UUID currentUserId = resolveCurrentUserId();

    GradeHistory history =
        GradeHistory.builder()
            .grade(grade)
            .oldValue(oldValue)
            .newValue(request.value())
            .reason(request.reason())
            .changedByUserId(currentUserId)
            .build();
    gradeHistoryRepository.save(history);

    grade.setValue(request.value());
    return GradeMapper.toResponse(gradeRepository.save(grade));
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

    BigDecimal weightedSum =
        publishedGrades.stream()
            .map(g -> g.getValue().multiply(g.getExam().getCoefficient()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalCoefficient =
        publishedGrades.stream()
            .map(g -> g.getExam().getCoefficient())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (totalCoefficient.compareTo(BigDecimal.ZERO) == 0) {
      return Optional.empty();
    }

    return Optional.of(weightedSum.divide(totalCoefficient, 2, RoundingMode.HALF_UP));
  }

  private UUID resolveCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return UUID.fromString(authentication.getName());
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
}
