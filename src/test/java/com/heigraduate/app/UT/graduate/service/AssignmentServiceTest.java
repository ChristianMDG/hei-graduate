package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.heigraduate.app.graduate.dto.AssignmentRequest;
import com.heigraduate.app.graduate.dto.AssignmentResponse;
import com.heigraduate.app.graduate.exception.BadRequestException;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Assignment;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.Group;
import com.heigraduate.app.graduate.model.Semester;
import com.heigraduate.app.graduate.model.Teacher;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.AssignmentRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.GroupRepository;
import com.heigraduate.app.graduate.repository.SemesterRepository;
import com.heigraduate.app.graduate.repository.TeacherRepository;
import com.heigraduate.app.graduate.service.AssignmentService;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

  @Mock private AssignmentRepository assignmentRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private TeacherRepository teacherRepository;
  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private SemesterRepository semesterRepository;
  @Mock private GroupRepository groupRepository;

  @InjectMocks private AssignmentService assignmentService;

  private Course prog4;
  private Teacher teacher;
  private AcademicYear year;
  private Group k1;
  private Group k2;
  private Group k3;

  @BeforeEach
  void setUp() {
    prog4 =
        Course.builder()
            .id(UUID.randomUUID())
            .courseReference("PROG4")
            .title("Programmation 4")
            .credits(5)
            .active(true)
            .build();

    teacher =
        Teacher.builder()
            .id(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .lastName("Rakoto")
            .firstName("Jean")
            .build();

    year =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 6, 30))
            .level("L2")
            .build();

    k1 = Group.builder().id(UUID.randomUUID()).reference("K1").capacity(30).active(true).build();
    k2 = Group.builder().id(UUID.randomUUID()).reference("K2").capacity(30).active(true).build();
    k3 = Group.builder().id(UUID.randomUUID()).reference("K3").capacity(30).active(true).build();
  }

  @Test
  void create_shouldAssignProg4ToK1AndK2ButNotK3() {
    AssignmentRequest request =
        new AssignmentRequest(
            prog4.getId(), teacher.getId(), year.getId(), null, Set.of(k1.getId(), k2.getId()));

    when(assignmentRepository.existsByCourseIdAndTeacherIdAndAcademicYearId(
            prog4.getId(), teacher.getId(), year.getId()))
        .thenReturn(false);
    when(courseRepository.findById(prog4.getId())).thenReturn(Optional.of(prog4));
    when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
    when(academicYearRepository.findById(year.getId())).thenReturn(Optional.of(year));
    when(groupRepository.findById(k1.getId())).thenReturn(Optional.of(k1));
    when(groupRepository.findById(k2.getId())).thenReturn(Optional.of(k2));
    when(assignmentRepository.save(any(Assignment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AssignmentResponse result = assignmentService.create(request);

    assertThat(result.courseReference()).isEqualTo("PROG4");
    assertThat(result.groups())
        .extracting(AssignmentResponse.GroupSummary::reference)
        .containsExactlyInAnyOrder("K1", "K2")
        .doesNotContain("K3");

    verify(groupRepository, never()).findById(k3.getId());
  }

  @Test
  void create_shouldThrowConflict_whenTeacherAlreadyAssignedToCourseForYear() {
    AssignmentRequest request =
        new AssignmentRequest(
            prog4.getId(), teacher.getId(), year.getId(), null, Set.of(k1.getId()));

    when(assignmentRepository.existsByCourseIdAndTeacherIdAndAcademicYearId(
            prog4.getId(), teacher.getId(), year.getId()))
        .thenReturn(true);

    assertThatThrownBy(() -> assignmentService.create(request))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("already assigned");

    verify(assignmentRepository, never()).save(any());
  }

  @Test
  void create_shouldThrowBadRequest_whenSemesterBelongsToAnotherAcademicYear() {
    AcademicYear otherYear =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2024-2025")
            .startDate(LocalDate.of(2024, 9, 1))
            .endDate(LocalDate.of(2025, 6, 30))
            .level("L1")
            .build();
    Semester semesterFromOtherYear =
        Semester.builder()
            .id(UUID.randomUUID())
            .academicYear(otherYear)
            .label("Semestre 1 (autre annee)")
            .startDate(LocalDate.of(2024, 9, 1))
            .endDate(LocalDate.of(2025, 1, 31))
            .expectedCredits(30)
            .active(true)
            .build();

    AssignmentRequest request =
        new AssignmentRequest(
            prog4.getId(),
            teacher.getId(),
            year.getId(),
            semesterFromOtherYear.getId(),
            Set.of(k1.getId()));

    when(assignmentRepository.existsByCourseIdAndTeacherIdAndAcademicYearId(
            prog4.getId(), teacher.getId(), year.getId()))
        .thenReturn(false);
    when(courseRepository.findById(prog4.getId())).thenReturn(Optional.of(prog4));
    when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
    when(academicYearRepository.findById(year.getId())).thenReturn(Optional.of(year));
    when(semesterRepository.findById(semesterFromOtherYear.getId()))
        .thenReturn(Optional.of(semesterFromOtherYear));

    assertThatThrownBy(() -> assignmentService.create(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("does not belong to academic year");

    verify(assignmentRepository, never()).save(any());
  }

  @Test
  void create_shouldThrow_whenCourseNotFound() {
    AssignmentRequest request =
        new AssignmentRequest(
            prog4.getId(), teacher.getId(), year.getId(), null, Set.of(k1.getId()));

    when(assignmentRepository.existsByCourseIdAndTeacherIdAndAcademicYearId(
            prog4.getId(), teacher.getId(), year.getId()))
        .thenReturn(false);
    when(courseRepository.findById(prog4.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> assignmentService.create(request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Course not found");
  }

  @Test
  void create_shouldThrow_whenGroupNotFound() {
    UUID unknownGroupId = UUID.randomUUID();
    AssignmentRequest request =
        new AssignmentRequest(
            prog4.getId(), teacher.getId(), year.getId(), null, Set.of(unknownGroupId));

    when(assignmentRepository.existsByCourseIdAndTeacherIdAndAcademicYearId(
            prog4.getId(), teacher.getId(), year.getId()))
        .thenReturn(false);
    when(courseRepository.findById(prog4.getId())).thenReturn(Optional.of(prog4));
    when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
    when(academicYearRepository.findById(year.getId())).thenReturn(Optional.of(year));
    when(groupRepository.findById(unknownGroupId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> assignmentService.create(request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Group not found");
  }

  @Test
  void delete_shouldThrow_whenAssignmentNotFound() {
    UUID unknownId = UUID.randomUUID();
    when(assignmentRepository.existsById(unknownId)).thenReturn(false);

    assertThatThrownBy(() -> assignmentService.delete(unknownId))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(assignmentRepository, never()).deleteById(any());
  }

  @Test
  void isTeacherAssignedToCourse_shouldReturnTrue_whenAssignmentExists() {
    when(assignmentRepository.existsByCourseIdAndTeacherIdAndAcademicYearId(
            prog4.getId(), teacher.getId(), year.getId()))
        .thenReturn(true);

    boolean result =
        assignmentService.isTeacherAssignedToCourse(teacher.getId(), prog4.getId(), year.getId());

    assertThat(result).isTrue();
  }

  @Test
  void isTeacherAssignedToCourse_shouldReturnFalse_whenNoAssignment() {
    when(assignmentRepository.existsByCourseIdAndTeacherIdAndAcademicYearId(
            prog4.getId(), teacher.getId(), year.getId()))
        .thenReturn(false);

    boolean result =
        assignmentService.isTeacherAssignedToCourse(teacher.getId(), prog4.getId(), year.getId());

    assertThat(result).isFalse();
  }
}
