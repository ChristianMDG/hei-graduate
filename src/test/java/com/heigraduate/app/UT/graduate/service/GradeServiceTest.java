package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.heigraduate.app.graduate.dto.GradeRequest;
import com.heigraduate.app.graduate.dto.GradeResponse;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.Exam;
import com.heigraduate.app.graduate.model.Grade;
import com.heigraduate.app.graduate.model.GradeStatus;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.model.Teacher;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.model.UserRole;
import com.heigraduate.app.graduate.repository.ExamRepository;
import com.heigraduate.app.graduate.repository.GradeRepository;
import com.heigraduate.app.graduate.repository.StudentRepository;
import com.heigraduate.app.graduate.repository.TeacherRepository;
import com.heigraduate.app.graduate.service.AssignmentService;
import com.heigraduate.app.graduate.service.GradeService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

  @Mock private GradeRepository gradeRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private ExamRepository examRepository;
  @Mock private TeacherRepository teacherRepository;
  @Mock private AssignmentService assignmentService;

  @InjectMocks private GradeService gradeService;

  private Student student;
  private Course course;
  private AcademicYear year;
  private Exam continuousControl;
  private Exam finalExam;
  private User actingUser;
  private Teacher teacher;

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

    course =
        Course.builder()
            .id(UUID.randomUUID())
            .courseReference("PROG4")
            .title("Programmation 4")
            .credits(5)
            .active(true)
            .build();

    year =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 6, 30))
            .level("L2")
            .build();

    continuousControl =
        Exam.builder()
            .id(UUID.randomUUID())
            .course(course)
            .academicYear(year)
            .label("Controle continu")
            .coefficientNumerator(2)
            .coefficientDenominator(5)
            .build();

    finalExam =
        Exam.builder()
            .id(UUID.randomUUID())
            .course(course)
            .academicYear(year)
            .label("Examen final")
            .coefficientNumerator(3)
            .coefficientDenominator(5)
            .build();

    actingUser =
        User.builder().id(UUID.randomUUID()).email("teacher@hei.mg").role(UserRole.TEACHER).build();

    teacher =
        Teacher.builder()
            .id(UUID.randomUUID())
            .userId(actingUser.getId())
            .lastName("Rakoto")
            .firstName("Jean")
            .build();
  }

  @Test
  void findPublishedForStudent_shouldOnlyReturnPublishedGrades() {
    Grade published =
        Grade.builder()
            .id(UUID.randomUUID())
            .student(student)
            .exam(continuousControl)
            .value(new BigDecimal("14.00"))
            .status(GradeStatus.PUBLISHED)
            .build();

    when(gradeRepository.findByStudentIdAndStatus(student.getId(), GradeStatus.PUBLISHED))
        .thenReturn(List.of(published));

    List<GradeResponse> result = gradeService.findPublishedForStudent(student.getId());

    assertThat(result).hasSize(1);
    assertThat(result.get(0).status()).isEqualTo(GradeStatus.PUBLISHED);

    verify(gradeRepository).findByStudentIdAndStatus(student.getId(), GradeStatus.PUBLISHED);
    verify(gradeRepository, never()).findByStudentId(any());
  }

  @Test
  void findMyPublishedGrades_shouldResolveStudentByUserId_thenReturnPublishedGrades() {
    Grade published =
        Grade.builder()
            .id(UUID.randomUUID())
            .student(student)
            .exam(continuousControl)
            .value(new BigDecimal("14.00"))
            .status(GradeStatus.PUBLISHED)
            .build();

    when(studentRepository.findByUserId(student.getUserId())).thenReturn(Optional.of(student));
    when(gradeRepository.findByStudentIdAndStatus(student.getId(), GradeStatus.PUBLISHED))
        .thenReturn(List.of(published));

    List<GradeResponse> result = gradeService.findMyPublishedGrades(student.getUserId());

    assertThat(result).hasSize(1);
    assertThat(result.get(0).status()).isEqualTo(GradeStatus.PUBLISHED);
  }

  @Test
  void findMyPublishedGrades_shouldThrow_whenNoStudentProfileLinkedToUser() {
    UUID orphanUserId = UUID.randomUUID();

    when(studentRepository.findByUserId(orphanUserId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> gradeService.findMyPublishedGrades(orphanUserId))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(gradeRepository, never()).findByStudentIdAndStatus(any(), any());
  }

  @Test
  void create_shouldThrowConflict_whenGradeAlreadyExistsForStudentAndExam() {
    GradeRequest request =
        new GradeRequest(student.getId(), continuousControl.getId(), new BigDecimal("15"));

    when(gradeRepository.existsByStudentIdAndExamId(student.getId(), continuousControl.getId()))
        .thenReturn(true);

    assertThatThrownBy(() -> gradeService.create(request, actingUser))
        .isInstanceOf(ConflictException.class);

    verify(gradeRepository, never()).save(any());
  }

  @Test
  void create_shouldDefaultToDraftStatus_whenTeacherIsAssignedToCourse() {
    GradeRequest request =
        new GradeRequest(student.getId(), continuousControl.getId(), new BigDecimal("15"));

    when(gradeRepository.existsByStudentIdAndExamId(student.getId(), continuousControl.getId()))
        .thenReturn(false);

    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));

    when(examRepository.findById(continuousControl.getId()))
        .thenReturn(Optional.of(continuousControl));

    when(teacherRepository.findByUserId(actingUser.getId())).thenReturn(Optional.of(teacher));

    when(assignmentService.isTeacherAssignedToCourse(teacher.getId(), course.getId(), year.getId()))
        .thenReturn(true);

    when(gradeRepository.save(any(Grade.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    GradeResponse result = gradeService.create(request, actingUser);

    assertThat(result.status()).isEqualTo(GradeStatus.DRAFT);

    verify(assignmentService)
        .isTeacherAssignedToCourse(teacher.getId(), course.getId(), year.getId());

    verify(gradeRepository).save(any(Grade.class));
  }

  @Test
  void create_shouldThrow403_whenTeacherHasNoTeacherProfileLinkedToUser() {
    GradeRequest request =
        new GradeRequest(student.getId(), continuousControl.getId(), new BigDecimal("15"));

    when(gradeRepository.existsByStudentIdAndExamId(student.getId(), continuousControl.getId()))
        .thenReturn(false);

    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));

    when(examRepository.findById(continuousControl.getId()))
        .thenReturn(Optional.of(continuousControl));

    when(teacherRepository.findByUserId(actingUser.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> gradeService.create(request, actingUser))
        .isInstanceOf(AccessDeniedException.class);

    // Regression guard: Assignment.teacherId references Teacher.id, never User.id.
    verify(assignmentService, never()).isTeacherAssignedToCourse(any(), any(), any());
    verify(gradeRepository, never()).save(any());
  }

  @Test
  void create_shouldThrow403_whenTeacherIsNotAssignedToCourse() {
    GradeRequest request =
        new GradeRequest(student.getId(), continuousControl.getId(), new BigDecimal("15"));

    when(gradeRepository.existsByStudentIdAndExamId(student.getId(), continuousControl.getId()))
        .thenReturn(false);

    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));

    when(examRepository.findById(continuousControl.getId()))
        .thenReturn(Optional.of(continuousControl));

    when(teacherRepository.findByUserId(actingUser.getId())).thenReturn(Optional.of(teacher));

    when(assignmentService.isTeacherAssignedToCourse(teacher.getId(), course.getId(), year.getId()))
        .thenReturn(false);

    assertThatThrownBy(() -> gradeService.create(request, actingUser))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("not assigned");

    verify(gradeRepository, never()).save(any());
  }

  @Test
  void create_shouldAllowAdmin_evenWhenAdminIsNotAssignedToCourse() {
    User admin =
        User.builder().id(UUID.randomUUID()).email("admin@hei.mg").role(UserRole.ADMIN).build();

    GradeRequest request =
        new GradeRequest(student.getId(), continuousControl.getId(), new BigDecimal("15"));

    when(gradeRepository.existsByStudentIdAndExamId(student.getId(), continuousControl.getId()))
        .thenReturn(false);

    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));

    when(examRepository.findById(continuousControl.getId()))
        .thenReturn(Optional.of(continuousControl));

    when(gradeRepository.save(any(Grade.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    GradeResponse result = gradeService.create(request, admin);

    assertThat(result.status()).isEqualTo(GradeStatus.DRAFT);

    verify(assignmentService, never()).isTeacherAssignedToCourse(any(), any(), any());

    verify(gradeRepository).save(any(Grade.class));
  }

  @Test
  void publish_shouldChangeStatusToPublished() {
    Grade draftGrade =
        Grade.builder()
            .id(UUID.randomUUID())
            .student(student)
            .exam(continuousControl)
            .value(new BigDecimal("14"))
            .status(GradeStatus.DRAFT)
            .build();

    when(gradeRepository.findById(draftGrade.getId())).thenReturn(Optional.of(draftGrade));

    when(gradeRepository.save(any(Grade.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    GradeResponse result = gradeService.publish(draftGrade.getId());

    assertThat(result.status()).isEqualTo(GradeStatus.PUBLISHED);
  }

  @Test
  void getFinalGrade_shouldComputeWeightedAverage_fromPublishedGradesOnly() {
    Grade controlGrade =
        Grade.builder()
            .student(student)
            .exam(continuousControl)
            .value(new BigDecimal("12.00"))
            .status(GradeStatus.PUBLISHED)
            .build();

    Grade finalGrade =
        Grade.builder()
            .student(student)
            .exam(finalExam)
            .value(new BigDecimal("16.00"))
            .status(GradeStatus.PUBLISHED)
            .build();

    when(gradeRepository.findPublishedByStudentIdAndCourseId(student.getId(), course.getId()))
        .thenReturn(List.of(controlGrade, finalGrade));

    Optional<BigDecimal> result = gradeService.getFinalGrade(student.getId(), course.getId());

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualByComparingTo("14.40");
  }

  @Test
  void getFinalGrade_shouldReturnEmpty_whenNoPublishedGrades() {
    when(gradeRepository.findPublishedByStudentIdAndCourseId(student.getId(), course.getId()))
        .thenReturn(List.of());

    Optional<BigDecimal> result = gradeService.getFinalGrade(student.getId(), course.getId());

    assertThat(result).isEmpty();
  }

  @Test
  void create_shouldThrow_whenStudentNotFound() {
    GradeRequest request =
        new GradeRequest(student.getId(), continuousControl.getId(), new BigDecimal("15"));

    when(gradeRepository.existsByStudentIdAndExamId(student.getId(), continuousControl.getId()))
        .thenReturn(false);

    when(studentRepository.findById(student.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> gradeService.create(request, actingUser))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
