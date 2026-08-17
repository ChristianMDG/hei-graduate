package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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
import com.heigraduate.app.graduate.service.EnrollmentService;
import com.heigraduate.app.graduate.service.TranscriptService;
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

@ExtendWith(MockitoExtension.class)
class TranscriptServiceTest {

  @Mock private StudentRepository studentRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private EnrollmentService enrollmentService;
  @Mock private CourseRequirementQuery courseRequirementQuery;
  @Mock private FinalGradeQuery finalGradeQuery;

  @InjectMocks private TranscriptService transcriptService;

  private Student student;
  private UUID trackId;
  private UUID academicYearId;
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

    trackId = UUID.randomUUID();
    academicYearId = UUID.randomUUID();

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

  @Test
  void generateTranscript_shouldProduceNonEmptyPdf_whenAllGradesPresent() {
    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(courseRequirementQuery.getMandatoryCourseIds(trackId, academicYearId))
        .thenReturn(List.of(course1.getId(), course2.getId()));
    when(courseRepository.findById(course1.getId())).thenReturn(Optional.of(course1));
    when(courseRepository.findById(course2.getId())).thenReturn(Optional.of(course2));
    when(finalGradeQuery.getFinalGrade(student.getId(), course1.getId()))
        .thenReturn(Optional.of(new BigDecimal("14.50")));
    when(finalGradeQuery.getFinalGrade(student.getId(), course2.getId()))
        .thenReturn(Optional.of(new BigDecimal("12.00")));

    byte[] pdf = transcriptService.generateTranscript(student.getId(), trackId, academicYearId);

    assertThat(pdf).isNotEmpty();
    assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
  }

  @Test
  void generateTranscript_shouldStillProducePdf_whenOneGradeIsMissing() {
    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(courseRequirementQuery.getMandatoryCourseIds(trackId, academicYearId))
        .thenReturn(List.of(course1.getId(), course2.getId()));
    when(courseRepository.findById(course1.getId())).thenReturn(Optional.of(course1));
    when(courseRepository.findById(course2.getId())).thenReturn(Optional.of(course2));
    when(finalGradeQuery.getFinalGrade(student.getId(), course1.getId()))
        .thenReturn(Optional.of(new BigDecimal("14.50")));
    when(finalGradeQuery.getFinalGrade(student.getId(), course2.getId()))
        .thenReturn(Optional.empty());

    byte[] pdf = transcriptService.generateTranscript(student.getId(), trackId, academicYearId);

    assertThat(pdf).isNotEmpty();
  }

  @Test
  void generateTranscript_shouldThrow_whenStudentNotFound() {
    when(studentRepository.findById(student.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> transcriptService.generateTranscript(student.getId(), trackId, academicYearId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void generateTranscriptForUser_shouldResolveTrackFromEnrollment_andYearFromToday() {
    Enrollment enrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(student.getId())
            .parcoursId(trackId)
            .groupId(UUID.randomUUID())
            .startDate(LocalDate.now().minusMonths(1))
            .endDate(null)
            .build();

    AcademicYear activeYear =
        AcademicYear.builder()
            .id(academicYearId)
            .label("2025-2026")
            .startDate(LocalDate.now().minusMonths(2))
            .endDate(LocalDate.now().plusMonths(6))
            .level("L2")
            .build();

    when(studentRepository.findByUserId(student.getUserId())).thenReturn(Optional.of(student));
    when(enrollmentService.getCurrent(student.getId())).thenReturn(enrollment);
    when(academicYearRepository.findAll()).thenReturn(List.of(activeYear));
    when(studentRepository.findById(student.getId())).thenReturn(Optional.of(student));
    when(courseRequirementQuery.getMandatoryCourseIds(trackId, academicYearId))
        .thenReturn(List.of());

    byte[] pdf = transcriptService.generateTranscriptForUser(student.getUserId());

    assertThat(pdf).isNotEmpty();
  }

  @Test
  void generateTranscriptForUser_shouldThrow_whenNoActiveAcademicYearFound() {
    Enrollment enrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(student.getId())
            .parcoursId(trackId)
            .groupId(UUID.randomUUID())
            .startDate(LocalDate.now().minusMonths(1))
            .endDate(null)
            .build();

    when(studentRepository.findByUserId(student.getUserId())).thenReturn(Optional.of(student));
    when(enrollmentService.getCurrent(student.getId())).thenReturn(enrollment);
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
