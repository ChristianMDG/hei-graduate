package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.contract.FinalGradeQuery;
import com.heigraduate.app.graduate.dto.AnnualAverageResult;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.CourseTrack;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.CourseTrackRepository;
import com.heigraduate.app.graduate.repository.EnrollmentRepository;
import com.heigraduate.app.graduate.service.AcademicAverageService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AcademicAverageServiceTest {

  @Mock private EnrollmentRepository enrollmentRepository;
  @Mock private CourseTrackRepository courseTrackRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private FinalGradeQuery finalGradeQuery;

  @InjectMocks private AcademicAverageService academicAverageService;

  private UUID studentId;
  private UUID parcoursId;
  private AcademicYear academicYear;

  @BeforeEach
  void setUp() {
    studentId = UUID.randomUUID();
    parcoursId = UUID.randomUUID();
    academicYear =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 6, 30))
            .level("L1")
            .build();
  }

  private CourseTrack courseTrack(UUID courseId, int credits) {
    Course course =
        Course.builder()
            .id(courseId)
            .courseReference("REF-" + courseId.toString().substring(0, 4))
            .title("Course")
            .credits(credits)
            .active(true)
            .build();
    return CourseTrack.builder().id(UUID.randomUUID()).course(course).mandatory(true).build();
  }

  @Test
  void computeAnnualAverage_shouldWeightByEctsCredits() {
    Enrollment enrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursId)
            .groupId(UUID.randomUUID())
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(null)
            .build();

    UUID course5Credits = UUID.randomUUID();
    UUID course3Credits = UUID.randomUUID();

    when(academicYearRepository.findById(academicYear.getId()))
        .thenReturn(Optional.of(academicYear));
    when(enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(enrollment));
    when(courseTrackRepository.findByTrackIdAndAcademicYearId(parcoursId, academicYear.getId()))
        .thenReturn(List.of(courseTrack(course5Credits, 5), courseTrack(course3Credits, 3)));
    when(finalGradeQuery.getFinalGrade(studentId, course5Credits))
        .thenReturn(Optional.of(new BigDecimal("16.00")));
    when(finalGradeQuery.getFinalGrade(studentId, course3Credits))
        .thenReturn(Optional.of(new BigDecimal("10.00")));

    AnnualAverageResult result =
        academicAverageService.computeAnnualAverage(studentId, academicYear.getId());

    // (16*5 + 10*3) / 8 = 13.75
    assertThat(result.average()).isEqualByComparingTo("13.75");
    assertThat(result.obtainedCredits()).isEqualTo(8);
    assertThat(result.expectedCredits()).isEqualTo(8);
    assertThat(result.validatedCourseIds())
        .containsExactlyInAnyOrder(course5Credits, course3Credits);
    assertThat(result.notValidatedCourseIds()).isEmpty();
    assertThat(result.isYearValidated()).isTrue();
  }

  @Test
  void
      computeAnnualAverage_shouldExcludeMissingGradesFromAverage_butKeepThemOutOfObtainedCredits() {
    Enrollment enrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursId)
            .groupId(UUID.randomUUID())
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(null)
            .build();

    UUID gradedCourse = UUID.randomUUID();
    UUID ungradedCourse = UUID.randomUUID();

    when(academicYearRepository.findById(academicYear.getId()))
        .thenReturn(Optional.of(academicYear));
    when(enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(enrollment));
    when(courseTrackRepository.findByTrackIdAndAcademicYearId(parcoursId, academicYear.getId()))
        .thenReturn(List.of(courseTrack(gradedCourse, 5), courseTrack(ungradedCourse, 5)));
    when(finalGradeQuery.getFinalGrade(studentId, gradedCourse))
        .thenReturn(Optional.of(new BigDecimal("12.00")));
    when(finalGradeQuery.getFinalGrade(studentId, ungradedCourse)).thenReturn(Optional.empty());

    AnnualAverageResult result =
        academicAverageService.computeAnnualAverage(studentId, academicYear.getId());

    assertThat(result.average()).isEqualByComparingTo("12.00");
    assertThat(result.obtainedCredits()).isEqualTo(5);
    assertThat(result.expectedCredits()).isEqualTo(10);
    assertThat(result.missingGradeCourseIds()).containsExactly(ungradedCourse);
    assertThat(result.isYearValidated())
        .as("expected credits not fully obtained while a grade is still missing")
        .isFalse();
  }

  @Test
  void computeAnnualAverage_shouldReconstructParcoursAcrossATransferWithinTheSameYear() {
    UUID parcoursBefore = UUID.randomUUID();
    UUID parcoursAfter = UUID.randomUUID();

    Enrollment beforeTransfer =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursBefore)
            .groupId(UUID.randomUUID())
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 1, 15))
            .build();
    Enrollment afterTransfer =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursAfter)
            .groupId(UUID.randomUUID())
            .startDate(LocalDate.of(2026, 1, 16))
            .endDate(null)
            .build();

    UUID courseBefore = UUID.randomUUID();
    UUID courseAfter = UUID.randomUUID();

    when(academicYearRepository.findById(academicYear.getId()))
        .thenReturn(Optional.of(academicYear));
    when(enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(beforeTransfer, afterTransfer));
    when(courseTrackRepository.findByTrackIdAndAcademicYearId(parcoursBefore, academicYear.getId()))
        .thenReturn(List.of(courseTrack(courseBefore, 4)));
    when(courseTrackRepository.findByTrackIdAndAcademicYearId(parcoursAfter, academicYear.getId()))
        .thenReturn(List.of(courseTrack(courseAfter, 6)));
    when(finalGradeQuery.getFinalGrade(studentId, courseBefore))
        .thenReturn(Optional.of(new BigDecimal("11.00")));
    when(finalGradeQuery.getFinalGrade(studentId, courseAfter))
        .thenReturn(Optional.of(new BigDecimal("15.00")));

    AnnualAverageResult result =
        academicAverageService.computeAnnualAverage(studentId, academicYear.getId());

    assertThat(result.expectedCredits())
        .as("courses from both parcours held during the year must be included")
        .isEqualTo(10);
    // (11*4 + 15*6) / 10 = 13.40
    assertThat(result.average()).isEqualByComparingTo("13.40");
  }

  @Test
  void computeAnnualAverage_shouldThrow_whenNoEnrollmentOverlapsTheYear() {
    when(academicYearRepository.findById(academicYear.getId()))
        .thenReturn(Optional.of(academicYear));
    when(enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId)).thenReturn(List.of());

    Assertions.assertThrows(
        ResourceNotFoundException.class,
        () -> academicAverageService.computeAnnualAverage(studentId, academicYear.getId()));
  }

  @Test
  void computeAnnualAverage_shouldThrow_whenAcademicYearNotFound() {
    UUID unknownYearId = UUID.randomUUID();
    when(academicYearRepository.findById(unknownYearId)).thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class,
        () -> academicAverageService.computeAnnualAverage(studentId, unknownYearId));
  }
}
