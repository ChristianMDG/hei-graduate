package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.contract.CourseRequirementQuery;
import com.heigraduate.app.graduate.contract.FinalGradeQuery;
import com.heigraduate.app.graduate.dto.GraduationResult;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.EnrollmentRepository;
import com.heigraduate.app.graduate.service.GraduationService;
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
class GraduationServiceTest {

  @Mock private EnrollmentRepository enrollmentRepository;
  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private CourseRequirementQuery courseRequirementQuery;
  @Mock private FinalGradeQuery finalGradeQuery;

  @InjectMocks private GraduationService graduationService;

  private UUID studentId;
  private UUID parcoursEl;
  private UUID parcoursTn;
  private UUID groupK1;
  private UUID groupK2;
  private UUID groupK3;
  private AcademicYear year1;
  private AcademicYear year2;
  private AcademicYear year3;

  @BeforeEach
  void setUp() {
    studentId = UUID.randomUUID();
    parcoursEl = UUID.randomUUID();
    parcoursTn = UUID.randomUUID();
    groupK1 = UUID.randomUUID();
    groupK2 = UUID.randomUUID();
    groupK3 = UUID.randomUUID();

    year1 =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2023-2024")
            .startDate(LocalDate.of(2023, 9, 1))
            .endDate(LocalDate.of(2024, 6, 30))
            .level("L1")
            .build();
    year2 =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2024-2025")
            .startDate(LocalDate.of(2024, 9, 1))
            .endDate(LocalDate.of(2025, 6, 30))
            .level("L2")
            .build();
    year3 =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 6, 30))
            .level("L3")
            .build();
  }

  private Course course(UUID id, int credits) {
    return Course.builder()
        .id(id)
        .courseReference("REF-" + id.toString().substring(0, 4))
        .title("Course")
        .credits(credits)
        .active(true)
        .build();
  }

  @Test
  void determineGraduation_shouldGraduate_studentWithGroupAndParcoursChangesOverThreeYears() {
    Enrollment year1Enrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursTn)
            .groupId(groupK3)
            .startDate(LocalDate.of(2023, 9, 1))
            .endDate(LocalDate.of(2024, 6, 30))
            .build();
    Enrollment year2Enrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursEl)
            .groupId(groupK1)
            .startDate(LocalDate.of(2024, 7, 1))
            .endDate(LocalDate.of(2025, 6, 30))
            .build();
    Enrollment year3Enrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursEl)
            .groupId(groupK2)
            .startDate(LocalDate.of(2025, 7, 1))
            .endDate(null)
            .build();

    when(enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(year1Enrollment, year2Enrollment, year3Enrollment));
    when(academicYearRepository.findAll()).thenReturn(List.of(year1, year2, year3));

    UUID tnCourseY1 = UUID.randomUUID();
    UUID elCourseY2 = UUID.randomUUID();
    UUID elCourseY3 = UUID.randomUUID();

    when(courseRequirementQuery.getMandatoryCourseIds(parcoursTn, year1.getId()))
        .thenReturn(List.of(tnCourseY1));
    when(courseRequirementQuery.getMandatoryCourseIds(parcoursEl, year2.getId()))
        .thenReturn(List.of(elCourseY2));
    when(courseRequirementQuery.getMandatoryCourseIds(parcoursEl, year3.getId()))
        .thenReturn(List.of(elCourseY3));

    when(finalGradeQuery.getFinalGrade(studentId, tnCourseY1))
        .thenReturn(Optional.of(new BigDecimal("12.00")));
    when(finalGradeQuery.getFinalGrade(studentId, elCourseY2))
        .thenReturn(Optional.of(new BigDecimal("14.00")));
    when(finalGradeQuery.getFinalGrade(studentId, elCourseY3))
        .thenReturn(Optional.of(new BigDecimal("10.00")));

    when(courseRepository.findById(tnCourseY1)).thenReturn(Optional.of(course(tnCourseY1, 5)));
    when(courseRepository.findById(elCourseY2)).thenReturn(Optional.of(course(elCourseY2, 5)));
    when(courseRepository.findById(elCourseY3)).thenReturn(Optional.of(course(elCourseY3, 5)));

    GraduationResult result = graduationService.determineGraduation(studentId);

    assertThat(result.graduated()).isTrue();
    assertThat(result.unvalidatedMandatoryCourseIds()).isEmpty();
    assertThat(result.academicYearIdsConsidered())
        .containsExactly(year1.getId(), year2.getId(), year3.getId());

    assertThat(result.overallAverage()).isEqualByComparingTo("12.00");
  }

  @Test
  void determineGraduation_shouldNotGraduate_whenOneMandatoryCourseBelowTen() {
    Enrollment enrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursEl)
            .groupId(groupK1)
            .startDate(LocalDate.of(2023, 9, 1))
            .endDate(null)
            .build();

    when(enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(enrollment));
    when(academicYearRepository.findAll()).thenReturn(List.of(year1, year2, year3));

    UUID failedCourse = UUID.randomUUID();
    when(courseRequirementQuery.getMandatoryCourseIds(parcoursEl, year1.getId()))
        .thenReturn(List.of(failedCourse));
    when(courseRequirementQuery.getMandatoryCourseIds(parcoursEl, year2.getId()))
        .thenReturn(List.of());
    when(courseRequirementQuery.getMandatoryCourseIds(parcoursEl, year3.getId()))
        .thenReturn(List.of());
    when(finalGradeQuery.getFinalGrade(studentId, failedCourse))
        .thenReturn(Optional.of(new BigDecimal("8.00")));

    GraduationResult result = graduationService.determineGraduation(studentId);

    assertThat(result.graduated()).isFalse();
    assertThat(result.unvalidatedMandatoryCourseIds()).containsExactly(failedCourse);
  }

  @Test
  void determineGraduation_shouldNotGraduate_whenFewerThanThreeAcademicYearsCompleted() {

    Enrollment enrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursEl)
            .groupId(groupK1)
            .startDate(LocalDate.of(2023, 9, 1))
            .endDate(LocalDate.of(2024, 6, 30))
            .build();

    when(enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(enrollment));
    when(academicYearRepository.findAll()).thenReturn(List.of(year1, year2, year3));

    UUID onlyCourse = UUID.randomUUID();
    when(courseRequirementQuery.getMandatoryCourseIds(parcoursEl, year1.getId()))
        .thenReturn(List.of(onlyCourse));
    when(finalGradeQuery.getFinalGrade(studentId, onlyCourse))
        .thenReturn(Optional.of(new BigDecimal("15.00")));
    when(courseRepository.findById(onlyCourse)).thenReturn(Optional.of(course(onlyCourse, 5)));

    GraduationResult result = graduationService.determineGraduation(studentId);

    assertThat(result.graduated())
        .as("a single completed academic year must never be enough to graduate")
        .isFalse();
    assertThat(result.academicYearIdsConsidered()).containsExactly(year1.getId());
  }

  @Test
  void determineGraduation_shouldNotGraduate_whenAMandatoryCourseHasNoFinalGradeYet() {
    Enrollment enrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursEl)
            .groupId(groupK1)
            .startDate(LocalDate.of(2023, 9, 1))
            .endDate(null)
            .build();

    when(enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(enrollment));
    when(academicYearRepository.findAll()).thenReturn(List.of(year1, year2, year3));

    UUID ungraduatedCourse = UUID.randomUUID();
    when(courseRequirementQuery.getMandatoryCourseIds(parcoursEl, year1.getId()))
        .thenReturn(List.of(ungraduatedCourse));
    when(courseRequirementQuery.getMandatoryCourseIds(parcoursEl, year2.getId()))
        .thenReturn(List.of());
    when(courseRequirementQuery.getMandatoryCourseIds(parcoursEl, year3.getId()))
        .thenReturn(List.of());
    when(finalGradeQuery.getFinalGrade(studentId, ungraduatedCourse)).thenReturn(Optional.empty());

    GraduationResult result = graduationService.determineGraduation(studentId);

    assertThat(result.graduated()).isFalse();
    assertThat(result.unvalidatedMandatoryCourseIds()).containsExactly(ungraduatedCourse);
  }

  @Test
  void determineGraduation_shouldThrow_whenStudentHasNoEnrollmentHistory() {
    when(enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId)).thenReturn(List.of());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> graduationService.determineGraduation(studentId));
  }

  @Test
  void determineGraduation_shouldNotGraduate_whenParcoursHasNoMandatoryCoursesAtAll() {
    Enrollment enrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursEl)
            .groupId(groupK1)
            .startDate(LocalDate.of(2023, 9, 1))
            .endDate(null)
            .build();

    when(enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(enrollment));
    when(academicYearRepository.findAll()).thenReturn(List.of(year1, year2, year3));
    when(courseRequirementQuery.getMandatoryCourseIds(any(), any())).thenReturn(List.of());

    GraduationResult result = graduationService.determineGraduation(studentId);

    assertThat(result.graduated())
        .as("zero mandatory courses found must never be read as 'nothing to fail'")
        .isFalse();
  }
}
