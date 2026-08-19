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

  /**
   * BUG-08 FIX — Retourne les notes d'un étudiant donné, filtrées aux cours de l'enseignant.
   *
   * <p>Un enseignant ne voit que les notes des cours auxquels il est affecté (§6/§18). L'ADMIN voit
   * toutes les notes sans restriction.
   */
  @Transactional(readOnly = true)
  public List<GradeResponse> findGradesForStudent(UUID studentId, User actingUser) {
    if ("ADMIN".equals(actingUser.getRole().name())) {
      return gradeRepository.findByStudentId(studentId).stream()
          .map(GradeMapper::toResponse)
          .toList();
    }

    // TEACHER : on filtre aux cours où il est affecté
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
      // BUG corrige : Assignment.teacher_id reference Teacher.id, pas User.id
      // (ce sont deux UUID distincts - cf. Teacher.userId). Utiliser
      // actingUser.getId() directement ici bloquait TOUS les enseignants,
      // meme correctement affectes.
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

  /**
   * BUG-09/10 FIX — publish() publie une note (la rend visible aux étudiants).
   *
   * <p>BUG-09 : Cette méthode est uniquement accessible à l'ADMIN (cf. GradeController). Un
   * enseignant ne peut pas publier les notes d'un cours auquel il n'est pas affecté.
   *
   * <p>BUG-10 : Avant de publier, on vérifie que la somme des coefficients des examens du cours +
   * semestre concerné est exactement égale à 1 (§7 cahier des charges). Si un examen manque encore,
   * la publication est bloquée.
   */
  @Transactional
  public GradeResponse publish(UUID id) {
    Grade grade =
        gradeRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + id));

    Exam exam = grade.getExam();

    // BUG-10 FIX : vérifier que la somme des coefficients = 1 avant publication
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

  /**
   * BUG-15 FIX — Calcule la note finale pondérée d'un étudiant pour un cours (§7/§8).
   *
   * <p>La division se fait par {@code totalWeight} (somme réelle des coefficients des examens
   * publiés). Quand tous les examens sont publiés et que leur somme vaut exactement 1 (garantie
   * depuis BUG-10), {@code totalWeight == 1} et la division est sans effet.
   *
   * <p>Si {@code totalWeight < 1} (tous les examens ne sont pas encore publiés), la note retournée
   * est une <b>note provisoire normalisée</b> : elle est légèrement surestime par rapport à la note
   * finale réelle. C'est un comportement accepté pour les relevés provisoires (type "PROVISOIRE"
   * dans la table transcript). La note finale réelle ne sera calculée qu'après la publication de
   * tous les examens du cours pour le semestre concerné.
   */
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

    // BUG-15 FIX : si totalWeight < 1, le résultat est une note provisoire normalisée.
    // Lorsque tous les examens seront publiés et que totalWeight == 1 (cf. BUG-10),
    // la division par totalWeight sera sans effet et la note finale sera exacte.
    return Optional.of(weightedSum.divide(totalWeight, 2, RoundingMode.HALF_UP));
  }
}
