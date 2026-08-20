package com.heigraduate.app.IT;

import static org.assertj.core.api.Assertions.assertThat;

import com.heigraduate.app.conf.FacadeIT;
import com.heigraduate.app.graduate.dto.DiplomaResponse;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.CourseTrack;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.model.Exam;
import com.heigraduate.app.graduate.model.Grade;
import com.heigraduate.app.graduate.model.GradeStatus;
import com.heigraduate.app.graduate.model.Group;
import com.heigraduate.app.graduate.model.Parcours;
import com.heigraduate.app.graduate.model.Promotion;
import com.heigraduate.app.graduate.model.Semester;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.model.UserRole;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.CourseTrackRepository;
import com.heigraduate.app.graduate.repository.EnrollmentRepository;
import com.heigraduate.app.graduate.repository.ExamRepository;
import com.heigraduate.app.graduate.repository.GradeRepository;
import com.heigraduate.app.graduate.repository.GroupRepository;
import com.heigraduate.app.graduate.repository.ParcoursRepository;
import com.heigraduate.app.graduate.repository.PromotionRepository;
import com.heigraduate.app.graduate.repository.SemesterRepository;
import com.heigraduate.app.graduate.repository.StudentRepository;
import com.heigraduate.app.graduate.repository.UserRepository;
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
class RankingIT extends FacadeIT {

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

  private AcademicYear year1;
  private AcademicYear year2;
  private AcademicYear year3;

  private Semester sem1;
  private Semester sem2;
  private Semester sem3;

  private Parcours parcoursEl;
  private Group group;
  private Promotion promotion;

  private Course c1;
  private Course c2;
  private Course c3;

  private Student studentA;
  private Student studentB;

  @BeforeEach
  void setUp() {
    String uid = UUID.randomUUID().toString().substring(0, 6);

    parcoursEl =
        parcoursRepository.save(
            Parcours.builder().code("EL-" + uid).label("Écosystème Logiciel").active(true).build());

    group =
        groupRepository.save(
            Group.builder().reference("K1-" + uid).capacity(30).active(true).build());

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

    sem1 = saveSemester(year1, "S1-2023");
    sem2 = saveSemester(year2, "S1-2024");
    sem3 = saveSemester(year3, "S1-2025");

    c1 = saveCourse("C1-" + uid, "Cours 1", 5);
    c2 = saveCourse("C2-" + uid, "Cours 2", 5);
    c3 = saveCourse("C3-" + uid, "Cours 3", 5);

    saveTrack(c1, parcoursEl, sem1);
    saveTrack(c2, parcoursEl, sem2);
    saveTrack(c3, parcoursEl, sem3);

    promotion =
        promotionRepository.save(
            Promotion.builder().label("Promo 2026-" + uid).finalAcademicYear(year3).build());

    studentA = createStudent("A-" + uid, "Rakoto", "Jean");
    studentB = createStudent("B-" + uid, "Rasoa", "Marie");
  }

  @Test
  void generateRanking_ordersByOverallAverage_descending() {
    enroll(studentA, LocalDate.of(2023, 9, 1));
    enroll(studentB, LocalDate.of(2023, 9, 1));

    publish(studentA, c1, year1, sem1, "16.00");
    publish(studentA, c2, year2, sem2, "16.00");
    publish(studentA, c3, year3, sem3, "16.00");

    publish(studentB, c1, year1, sem1, "14.00");
    publish(studentB, c2, year2, sem2, "14.00");
    publish(studentB, c3, year3, sem3, "14.00");

    List<DiplomaResponse> ranking =
        rankingService.generateRanking(promotion.getId(), parcoursEl.getId());

    assertThat(ranking).hasSize(2);

    assertThat(ranking.get(0).rank()).isEqualTo(1);
    assertThat(ranking.get(0).studentNumber()).isEqualTo(studentA.getStudentNumber());
    assertThat(ranking.get(0).overallAverage()).isEqualByComparingTo("16.00");
    assertThat(ranking.get(0).mention()).isEqualTo("Tres Bien");

    assertThat(ranking.get(1).rank()).isEqualTo(2);
    assertThat(ranking.get(1).studentNumber()).isEqualTo(studentB.getStudentNumber());
    assertThat(ranking.get(1).overallAverage()).isEqualByComparingTo("14.00");
    assertThat(ranking.get(1).mention()).isEqualTo("Bien");
  }

  @Test
  void generateRanking_excludesNonGraduates() {
    enroll(studentA, LocalDate.of(2023, 9, 1));
    enroll(studentB, LocalDate.of(2023, 9, 1));

    publish(studentA, c1, year1, sem1, "15.00");
    publish(studentA, c2, year2, sem2, "15.00");
    publish(studentA, c3, year3, sem3, "15.00");

    publish(studentB, c1, year1, sem1, "8.00");
    publish(studentB, c2, year2, sem2, "12.00");
    publish(studentB, c3, year3, sem3, "12.00");

    List<DiplomaResponse> ranking =
        rankingService.generateRanking(promotion.getId(), parcoursEl.getId());

    assertThat(ranking).hasSize(1);

    assertThat(ranking.get(0).studentNumber()).isEqualTo(studentA.getStudentNumber());
  }

  @Test
  void getRanking_returnsPersistedOrder() {
    enroll(studentA, LocalDate.of(2023, 9, 1));

    publish(studentA, c1, year1, sem1, "13.00");
    publish(studentA, c2, year2, sem2, "13.00");
    publish(studentA, c3, year3, sem3, "13.00");

    rankingService.generateRanking(promotion.getId(), parcoursEl.getId());

    List<DiplomaResponse> ranking =
        rankingService.getRanking(promotion.getId(), parcoursEl.getId());

    assertThat(ranking).hasSize(1);

    assertThat(ranking.get(0).rank()).isEqualTo(1);
    assertThat(ranking.get(0).mention()).isEqualTo("Assez Bien");
  }

  private Student createStudent(String num, String last, String first) {

    User user =
        userRepository.save(
            User.builder()
                .email(num + "@hei.mg")
                .password("{noop}pwd")
                .role(UserRole.STUDENT)
                .active(true)
                .build());

    return studentRepository.save(
        Student.builder()
            .userId(user.getId())
            .studentNumber("STD-" + num)
            .lastName(last)
            .firstName(first)
            .enrollmentDate(LocalDate.of(2023, 9, 1))
            .status("ACTIVE")
            .build());
  }

  private void enroll(Student s, LocalDate start) {
    enrollmentRepository.save(
        Enrollment.builder()
            .studentId(s.getId())
            .parcoursId(parcoursEl.getId())
            .groupId(group.getId())
            .startDate(start)
            .endDate(null)
            .build());
  }

  private Semester saveSemester(AcademicYear year, String label) {

    return semesterRepository.save(
        Semester.builder()
            .academicYear(year)
            .label(label)
            .startDate(year.getStartDate())
            .endDate(year.getEndDate())
            .expectedCredits(30)
            .active(true)
            .build());
  }

  private Course saveCourse(String ref, String title, int credits) {

    return courseRepository.save(
        Course.builder().courseReference(ref).title(title).credits(credits).active(true).build());
  }

  private void saveTrack(Course course, Parcours track, Semester semester) {

    courseTrackRepository.save(
        CourseTrack.builder()
            .course(course)
            .track(track)
            .semester(semester)
            .mandatory(true)
            .build());
  }

  private void publish(
      Student student, Course course, AcademicYear year, Semester semester, String value) {

    Exam exam =
        examRepository.save(
            Exam.builder()
                .course(course)
                .academicYear(year)
                .semester(semester)
                .label("Exam " + course.getCourseReference())
                .date(year.getStartDate().plusMonths(3))
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
