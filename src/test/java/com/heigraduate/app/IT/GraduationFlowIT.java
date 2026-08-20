package com.heigraduate.app.IT;

import static org.assertj.core.api.Assertions.assertThat;

import com.heigraduate.app.conf.FacadeIT;
import com.heigraduate.app.graduate.dto.DiplomaResponse;
import com.heigraduate.app.graduate.dto.GraduationResult;
import com.heigraduate.app.graduate.model.*;
import com.heigraduate.app.graduate.repository.*;
import com.heigraduate.app.graduate.service.GraduationService;
import com.heigraduate.app.graduate.service.RankingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class GraduationFlowIT extends FacadeIT {

  @Autowired private GraduationService graduationService;
  @Autowired private RankingService rankingService;

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
  @Autowired private PromotionRepository promotionRepository;

  private AcademicYear year1, year2, year3;
  private Semester s1y1, s1y2, s1y3;
  private Parcours parcoursTn, parcoursEl;
  private Group groupK1, groupK2, groupK3;
  private Course courseTnY1, courseElY2, courseElY3;
  private Student student;
  private Promotion promotion;
  private Grade gradeY3;

  @BeforeEach
  void setUp() {
    parcoursTn =
        parcoursRepository.save(
            Parcours.builder().code("TN").label("Transformation Numérique").active(true).build());
    parcoursEl =
        parcoursRepository.save(
            Parcours.builder().code("EL").label("Écosystème Logiciel").active(true).build());

    groupK1 =
        groupRepository.save(Group.builder().reference("K1").capacity(30).active(true).build());
    groupK2 =
        groupRepository.save(Group.builder().reference("K2").capacity(30).active(true).build());
    groupK3 =
        groupRepository.save(Group.builder().reference("K3").capacity(30).active(true).build());

    year1 =
        academicYearRepository.save(
            AcademicYear.builder()
                .label("2023-2024")
                .startDate(LocalDate.of(2023, 9, 1))
                .endDate(LocalDate.of(2024, 6, 30))
                .level("L1")
                .build());
    year2 =
        academicYearRepository.save(
            AcademicYear.builder()
                .label("2024-2025")
                .startDate(LocalDate.of(2024, 9, 1))
                .endDate(LocalDate.of(2025, 6, 30))
                .level("L2")
                .build());
    year3 =
        academicYearRepository.save(
            AcademicYear.builder()
                .label("2025-2026")
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .level("L3")
                .build());

    s1y1 = saveSemester(year1, "S1-2023", LocalDate.of(2023, 9, 1), LocalDate.of(2024, 1, 31));
    s1y2 = saveSemester(year2, "S1-2024", LocalDate.of(2024, 9, 1), LocalDate.of(2025, 1, 31));
    s1y3 = saveSemester(year3, "S1-2025", LocalDate.of(2025, 9, 1), LocalDate.of(2026, 1, 31));

    courseTnY1 = saveCourse("METIER1", "Métier TN", 5);
    courseElY2 = saveCourse("PROG4", "Programmation avancée", 5);
    courseElY3 = saveCourse("ARCHI1", "Architecture logicielle", 5);

    saveCourseTrack(courseTnY1, parcoursTn, s1y1);
    saveCourseTrack(courseElY2, parcoursEl, s1y2);
    saveCourseTrack(courseElY3, parcoursEl, s1y3);

    Exam examY1 = saveExam(courseTnY1, year1, s1y1, "Exam METIER1");
    Exam examY2 = saveExam(courseElY2, year2, s1y2, "Exam PROG4");
    Exam examY3 = saveExam(courseElY3, year3, s1y3, "Exam ARCHI1");

    String unique = UUID.randomUUID().toString().substring(0, 8);
    User user =
        userRepository.save(
            User.builder()
                .email("std-" + unique + "@hei.mg")
                .password("{noop}password")
                .role(UserRole.STUDENT)
                .active(true)
                .build());

    student =
        studentRepository.save(
            Student.builder()
                .userId(user.getId())
                .studentNumber("STD-" + unique)
                .lastName("Rakoto")
                .firstName("Jean")
                .enrollmentDate(LocalDate.of(2023, 9, 1))
                .status("ACTIVE")
                .build());

    enrollmentRepository.save(
        Enrollment.builder()
            .studentId(student.getId())
            .parcoursId(parcoursTn.getId())
            .groupId(groupK3.getId())
            .startDate(LocalDate.of(2023, 9, 1))
            .endDate(LocalDate.of(2024, 6, 30))
            .build());
    enrollmentRepository.save(
        Enrollment.builder()
            .studentId(student.getId())
            .parcoursId(parcoursEl.getId())
            .groupId(groupK1.getId())
            .startDate(LocalDate.of(2024, 7, 1))
            .endDate(LocalDate.of(2025, 6, 30))
            .build());
    enrollmentRepository.save(
        Enrollment.builder()
            .studentId(student.getId())
            .parcoursId(parcoursEl.getId())
            .groupId(groupK2.getId())
            .startDate(LocalDate.of(2025, 7, 1))
            .endDate(null)
            .build());

    gradeRepository.save(buildGrade(student, examY1, "12.00"));
    gradeRepository.save(buildGrade(student, examY2, "14.00"));
    gradeY3 = gradeRepository.save(buildGrade(student, examY3, "10.00"));

    promotion =
        promotionRepository.save(
            Promotion.builder().label("Promotion 2026").finalAcademicYear(year3).build());
  }

  private Semester saveSemester(AcademicYear year, String label, LocalDate start, LocalDate end) {
    return semesterRepository.save(
        Semester.builder()
            .academicYear(year)
            .label(label)
            .startDate(start)
            .endDate(end)
            .expectedCredits(30)
            .active(true)
            .build());
  }

  private Course saveCourse(String ref, String title, int credits) {
    return courseRepository.save(
        Course.builder()
            .courseReference(ref + "-" + UUID.randomUUID().toString().substring(0, 4))
            .title(title)
            .credits(credits)
            .active(true)
            .build());
  }

  private void saveCourseTrack(Course course, Parcours track, Semester semester) {
    courseTrackRepository.save(
        CourseTrack.builder()
            .course(course)
            .track(track)
            .semester(semester)
            .mandatory(true)
            .build());
  }

  private Exam saveExam(Course course, AcademicYear year, Semester semester, String label) {
    return examRepository.save(
        Exam.builder()
            .course(course)
            .academicYear(year)
            .semester(semester)
            .label(label)
            .date(semester.getStartDate().plusMonths(3))
            .startTime(LocalTime.of(8, 0))
            .endTime(LocalTime.of(10, 0))
            .coefficientNumerator(1)
            .coefficientDenominator(1)
            .build());
  }

  private Grade buildGrade(Student s, Exam exam, String value) {
    return Grade.builder()
        .student(s)
        .exam(exam)
        .value(new BigDecimal(value))
        .status(GradeStatus.PUBLISHED)
        .build();
  }

  // ---------- tests ----------

  @Test
  void shouldGraduate_whenStudentChangedGroupAndParcours_andAllMandatoryCoursesValidated() {
    GraduationResult result = graduationService.determineGraduation(student.getId());

    assertThat(result.graduated()).isTrue();
    assertThat(result.unvalidatedMandatoryCourseIds()).isEmpty();
    assertThat(result.academicYearIdsConsidered())
        .containsExactly(year1.getId(), year2.getId(), year3.getId());
    assertThat(result.overallAverage()).isEqualByComparingTo("12.00");
  }

  @Test
  void shouldNotGraduate_whenOneMandatoryCourseBelowTen() {
    gradeY3.setValue(new BigDecimal("8.00"));
    gradeRepository.save(gradeY3);

    GraduationResult result = graduationService.determineGraduation(student.getId());

    assertThat(result.graduated()).isFalse();
    assertThat(result.unvalidatedMandatoryCourseIds()).contains(courseElY3.getId());
  }

  @Test
  void shouldNotGraduate_whenFewerThanThreeYears() {
    // On ne garde que l'enrollment L1
    enrollmentRepository.findByStudentIdOrderByStartDateAsc(student.getId()).stream()
        .filter(e -> e.getEndDate() == null || e.getStartDate().isAfter(LocalDate.of(2024, 1, 1)))
        .forEach(enrollmentRepository::delete);

    GraduationResult result = graduationService.determineGraduation(student.getId());

    assertThat(result.graduated()).isFalse();
    assertThat(result.academicYearIdsConsidered()).containsExactly(year1.getId());
  }

  @Test
  void shouldGenerateRanking_forPromotionAndParcours() {
    List<DiplomaResponse> ranking =
        rankingService.generateRanking(promotion.getId(), parcoursEl.getId());

    assertThat(ranking).hasSize(1);
    DiplomaResponse first = ranking.get(0);
    assertThat(first.studentNumber()).isEqualTo(student.getStudentNumber());
    assertThat(first.rank()).isEqualTo(1);
    assertThat(first.overallAverage()).isEqualByComparingTo("12.00");
    assertThat(first.mention()).isEqualTo("Assez Bien");
    assertThat(first.lastName()).isEqualTo("Rakoto");
    assertThat(first.firstName()).isEqualTo("Jean");
  }
}
