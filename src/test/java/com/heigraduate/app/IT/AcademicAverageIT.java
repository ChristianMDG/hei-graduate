package com.heigraduate.app.IT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.heigraduate.app.conf.FacadeIT;
import com.heigraduate.app.graduate.dto.AnnualAverageResult;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.*;
import com.heigraduate.app.graduate.repository.*;
import com.heigraduate.app.graduate.service.AcademicAverageService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


@Transactional
class AcademicAverageIT extends FacadeIT {

  @Autowired private AcademicAverageService academicAverageService;

  @Autowired private UserRepository userRepository;
  @Autowired private StudentRepository studentRepository;
  @Autowired private ParcoursRepository parcoursRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private AcademicYearRepository academicYearRepository;
  @Autowired private SemesterRepository semesterRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private CourseTrackRepository courseTrackRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeRepository gradeRepository;
  @Autowired private EnrollmentRepository enrollmentRepository;

  private AcademicYear academicYear;
  private Semester semester;
  private Parcours parcoursEl;
  private Parcours parcoursTn;
  private Group groupK1;
  private Group groupK2;
  private Student student;

  private Course course5Credits; // EL
  private Course course3Credits; // EL
  private Course courseTnOnly; // TN uniquement

  @BeforeEach
  void setUp() {
    String uid = UUID.randomUUID().toString().substring(0, 8);

    parcoursEl =
        parcoursRepository.save(
            Parcours.builder().code("EL-" + uid).label("Écosystème Logiciel").active(true).build());
    parcoursTn =
        parcoursRepository.save(
            Parcours.builder()
                .code("TN-" + uid)
                .label("Transformation Numérique")
                .active(true)
                .build());

    groupK1 =
        groupRepository.save(
            Group.builder().reference("K1-" + uid).capacity(30).active(true).build());
    groupK2 =
        groupRepository.save(
            Group.builder().reference("K2-" + uid).capacity(30).active(true).build());

    academicYear =
        academicYearRepository.save(
            AcademicYear.builder()
                .label("2025-2026")
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .level("L1")
                .build());

    semester =
        semesterRepository.save(
            Semester.builder()
                .academicYear(academicYear)
                .label("S1-2025")
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2026, 1, 31))
                .expectedCredits(30)
                .active(true)
                .build());

    course5Credits = saveCourse("PROG4-" + uid, "Programmation", 5);
    course3Credits = saveCourse("WEB1-" + uid, "Web", 3);
    courseTnOnly = saveCourse("METIER1-" + uid, "Métier TN", 4);

    // CourseTrack EL
    saveTrack(course5Credits, parcoursEl);
    saveTrack(course3Credits, parcoursEl);
    // CourseTrack TN
    saveTrack(courseTnOnly, parcoursTn);

    User user =
        userRepository.save(
            User.builder()
                .email("std-" + uid + "@hei.mg")
                .password("{noop}pwd")
                .role(UserRole.STUDENT)
                .active(true)
                .build());

    student =
        studentRepository.save(
            Student.builder()
                .userId(user.getId())
                .studentNumber("STD-" + uid)
                .lastName("Rakoto")
                .firstName("Jean")
                .enrollmentDate(LocalDate.of(2025, 9, 1))
                .status("ACTIVE")
                .build());
  }


  @Test
  void computeAnnualAverage_shouldWeightByEctsCredits() {
   
    enroll(parcoursEl.getId(), groupK1.getId(), LocalDate.of(2025, 9, 1), null);

    publishFinalGrade(course5Credits, "16.00");
    publishFinalGrade(course3Credits, "10.00");

    AnnualAverageResult result =
        academicAverageService.computeAnnualAverage(student.getId(), academicYear.getId());

    assertThat(result.average()).isEqualByComparingTo("13.75");
    assertThat(result.obtainedCredits()).isEqualTo(8);
    assertThat(result.expectedCredits()).isEqualTo(8);
    assertThat(result.validatedCourseIds())
        .containsExactlyInAnyOrder(course5Credits.getId(), course3Credits.getId());
    assertThat(result.notValidatedCourseIds()).isEmpty();
    assertThat(result.missingGradeCourseIds()).isEmpty();
    assertThat(result.isYearValidated()).isTrue();
  }

  @Test
  void computeAnnualAverage_shouldLowerAverage_whenGradeMissing() {
    enroll(parcoursEl.getId(), groupK1.getId(), LocalDate.of(2025, 9, 1), null);

    publishFinalGrade(course5Credits, "12.00");

    AnnualAverageResult result =
        academicAverageService.computeAnnualAverage(student.getId(), academicYear.getId());

    assertThat(result.average()).isEqualByComparingTo("7.50");
    assertThat(result.obtainedCredits()).isEqualTo(5);
    assertThat(result.expectedCredits()).isEqualTo(8);
    assertThat(result.missingGradeCourseIds()).containsExactly(course3Credits.getId());
    assertThat(result.isYearValidated()).isFalse();
  }



  @Test
  void computeAnnualAverage_shouldIncludeCoursesFromBothParcours_afterTransfer() {
    enroll(
        parcoursTn.getId(),
        groupK2.getId(),
        LocalDate.of(2025, 9, 1),
        LocalDate.of(2026, 1, 15));

    enroll(parcoursEl.getId(), groupK1.getId(), LocalDate.of(2026, 1, 16), null);

    publishFinalGrade(courseTnOnly, "11.00"); 
    publishFinalGrade(course5Credits, "15.00");
    publishFinalGrade(course3Credits, "13.00");

    AnnualAverageResult result =
        academicAverageService.computeAnnualAverage(student.getId(), academicYear.getId());

    assertThat(result.expectedCredits()).isEqualTo(12);
    assertThat(result.average()).isEqualByComparingTo("13.17");
    assertThat(result.obtainedCredits()).isEqualTo(12);
    assertThat(result.validatedCourseIds())
        .containsExactlyInAnyOrder(
            courseTnOnly.getId(), course5Credits.getId(), course3Credits.getId());
    assertThat(result.isYearValidated()).isTrue();
  }


  @Test
  void computeAnnualAverage_shouldMarkCourseAsNotValidated_whenBelowTen() {
    enroll(parcoursEl.getId(), groupK1.getId(), LocalDate.of(2025, 9, 1), null);

    publishFinalGrade(course5Credits, "16.00");
    publishFinalGrade(course3Credits, "8.00"); // non validé

    AnnualAverageResult result =
        academicAverageService.computeAnnualAverage(student.getId(), academicYear.getId());

    // (16*5 + 8*3) / 8 = 104 / 8 = 13.00
    assertThat(result.average()).isEqualByComparingTo("13.00");
    assertThat(result.validatedCourseIds()).containsExactly(course5Credits.getId());
    assertThat(result.notValidatedCourseIds()).containsExactly(course3Credits.getId());
    assertThat(result.obtainedCredits()).isEqualTo(5); // seuls les validés
    assertThat(result.expectedCredits()).isEqualTo(8);
    assertThat(result.isYearValidated()).isFalse();
  }

  // =========================================================================
  // 5. Cours TN n'apparaît PAS pour un étudiant EL pur
  // =========================================================================

  @Test
  void computeAnnualAverage_shouldNotIncludeTnOnlyCourse_forElStudent() {
    enroll(parcoursEl.getId(), groupK1.getId(), LocalDate.of(2025, 9, 1), null);

    publishFinalGrade(course5Credits, "14.00");
    publishFinalGrade(course3Credits, "12.00");
    // Note TN existante mais hors parcours de l'étudiant
    publishFinalGrade(courseTnOnly, "18.00");

    AnnualAverageResult result =
        academicAverageService.computeAnnualAverage(student.getId(), academicYear.getId());

    assertThat(result.expectedCredits()).isEqualTo(8);
    assertThat(result.validatedCourseIds())
        .containsExactlyInAnyOrder(course5Credits.getId(), course3Credits.getId())
        .doesNotContain(courseTnOnly.getId());
    // (14*5 + 12*3) / 8 = 106 / 8 = 13.25
    assertThat(result.average()).isEqualByComparingTo("13.25");
  }

  // =========================================================================
  // 6. Pas d'enrollment
  // =========================================================================

  @Test
  void computeAnnualAverage_shouldThrow_whenNoEnrollmentOverlapsTheYear() {
    // Aucun enrollment créé

    assertThatThrownBy(
            () ->
                academicAverageService.computeAnnualAverage(
                    student.getId(), academicYear.getId()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("No enrollment found");
  }

  @Test
  void computeAnnualAverage_shouldThrow_whenAcademicYearNotFound() {
    enroll(parcoursEl.getId(), groupK1.getId(), LocalDate.of(2025, 9, 1), null);

    assertThatThrownBy(
            () ->
                academicAverageService.computeAnnualAverage(
                    student.getId(), UUID.randomUUID()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("AcademicYear not found");
  }

  // ---------- helpers ----------

  private Course saveCourse(String ref, String title, int credits) {
    return courseRepository.save(
        Course.builder().courseReference(ref).title(title).credits(credits).active(true).build());
  }

  private void saveTrack(Course course, Parcours track) {
    courseTrackRepository.save(
        CourseTrack.builder()
            .course(course)
            .track(track)
            .semester(semester)
            .mandatory(true)
            .build());
  }

  private void enroll(UUID parcoursId, UUID groupId, LocalDate start, LocalDate end) {
    enrollmentRepository.save(
        Enrollment.builder()
            .studentId(student.getId())
            .parcoursId(parcoursId)
            .groupId(groupId)
            .startDate(start)
            .endDate(end)
            .build());
  }

  /**
   * Crée un examen coef 1/1 + note PUBLISHED → getFinalGrade retourne la valeur.
   */
  private void publishFinalGrade(Course course, String value) {
    Exam exam =
        examRepository.save(
            Exam.builder()
                .course(course)
                .academicYear(academicYear)
                .semester(semester)
                .label("Exam " + course.getCourseReference())
                .date(LocalDate.of(2025, 12, 1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .coefficientNumerator(1)
                .coefficientDenominator(1)
                .build());

    gradeRepository.save(
        Grade.builder()
            .student(student)
            .exam(exam)
            .value(new BigDecimal(value))
            .status(GradeStatus.PUBLISHED)
            .build());
  }
}
