package com.heigraduate.app.IT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.heigraduate.app.conf.FacadeIT;
import com.heigraduate.app.graduate.dto.GradeHistoryResponse;
import com.heigraduate.app.graduate.dto.GradeRequest;
import com.heigraduate.app.graduate.dto.GradeResponse;
import com.heigraduate.app.graduate.dto.GradeUpdateRequest;
import com.heigraduate.app.graduate.exception.BadRequestException;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.model.*;
import com.heigraduate.app.graduate.repository.*;
import com.heigraduate.app.graduate.service.GradeService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class GradeFlowIT extends FacadeIT {

  @Autowired private GradeService gradeService;

  @Autowired private UserRepository userRepository;
  @Autowired private StudentRepository studentRepository;
  @Autowired private TeacherRepository teacherRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private AcademicYearRepository academicYearRepository;
  @Autowired private SemesterRepository semesterRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private AssignmentRepository assignmentRepository;
  @Autowired private GradeRepository gradeRepository;

  private User adminUser;
  private User teacherUser;
  private User otherTeacherUser;
  private User studentUser;

  private Teacher teacher;
  private Teacher otherTeacher;
  private Student student;

  private Course course;
  private AcademicYear academicYear;
  private Semester semester;
  private Group group;

  private Exam examHalf;

  private Exam examQuarter1;
  private Exam examQuarter2;

  @BeforeEach
  void setUp() {
    String uid = UUID.randomUUID().toString().substring(0, 8);

    // --- Users ---
    adminUser =
        userRepository.save(
            User.builder()
                .email("admin-" + uid + "@hei.mg")
                .password("{noop}pwd")
                .role(UserRole.ADMIN)
                .active(true)
                .build());
    teacherUser =
        userRepository.save(
            User.builder()
                .email("teacher-" + uid + "@hei.mg")
                .password("{noop}pwd")
                .role(UserRole.TEACHER)
                .active(true)
                .build());
    otherTeacherUser =
        userRepository.save(
            User.builder()
                .email("other-teacher-" + uid + "@hei.mg")
                .password("{noop}pwd")
                .role(UserRole.TEACHER)
                .active(true)
                .build());
    studentUser =
        userRepository.save(
            User.builder()
                .email("student-" + uid + "@hei.mg")
                .password("{noop}pwd")
                .role(UserRole.STUDENT)
                .active(true)
                .build());

    // --- Teacher / Student profiles ---
    teacher =
        teacherRepository.save(
            Teacher.builder()
                .userId(teacherUser.getId())
                .lastName("Randria")
                .firstName("Paul")
                .specialty("Prog")
                .build());
    otherTeacher =
        teacherRepository.save(
            Teacher.builder()
                .userId(otherTeacherUser.getId())
                .lastName("Rasoa")
                .firstName("Marie")
                .specialty("Web")
                .build());
    student =
        studentRepository.save(
            Student.builder()
                .userId(studentUser.getId())
                .studentNumber("STD-" + uid)
                .lastName("Rakoto")
                .firstName("Jean")
                .enrollmentDate(LocalDate.of(2025, 9, 1))
                .status("ACTIVE")
                .build());

    // --- Référentiel ---
    academicYear =
        academicYearRepository.save(
            AcademicYear.builder()
                .label("2025-2026")
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .level("L3")
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
    group =
        groupRepository.save(
            Group.builder().reference("K1-" + uid).capacity(30).active(true).build());
    course =
        courseRepository.save(
            Course.builder()
                .courseReference("PROG4-" + uid)
                .title("Programmation avancée")
                .credits(5)
                .active(true)
                .build());

    // --- Affectation : teacher → course (otherTeacher NON affecté) ---
    assignmentRepository.save(
        Assignment.builder()
            .course(course)
            .teacher(teacher)
            .academicYear(academicYear)
            .semester(semester)
            .groups(Set.of(group))
            .build());

    // --- Examens (somme coef = 1) ---
    examHalf = saveExam("Partiel", 1, 2);
    examQuarter1 = saveExam("TP1", 1, 4);
    examQuarter2 = saveExam("TP2", 1, 4);
  }

  private Exam saveExam(String label, int num, int den) {
    return examRepository.save(
        Exam.builder()
            .course(course)
            .academicYear(academicYear)
            .semester(semester)
            .label(label)
            .date(LocalDate.of(2025, 11, 15))
            .startTime(LocalTime.of(8, 0))
            .endTime(LocalTime.of(10, 0))
            .coefficientNumerator(num)
            .coefficientDenominator(den)
            .build());
  }

  // =========================================================================
  // 1. Création
  // =========================================================================

  @Test
  void admin_canCreateGrade() {
    GradeResponse response =
        gradeService.create(
            new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("14.50")),
            adminUser);

    assertThat(response.id()).isNotNull();
    assertThat(response.value()).isEqualByComparingTo("14.50");
    assertThat(response.status()).isEqualTo(GradeStatus.DRAFT);
    assertThat(response.studentId()).isEqualTo(student.getId());
    assertThat(response.examId()).isEqualTo(examHalf.getId());
    assertThat(response.enteredByUserId()).isEqualTo(adminUser.getId());
  }

  @Test
  void assignedTeacher_canCreateGrade() {
    GradeResponse response =
        gradeService.create(
            new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("12.00")),
            teacherUser);

    assertThat(response.status()).isEqualTo(GradeStatus.DRAFT);
    assertThat(response.enteredByUserId()).isEqualTo(teacherUser.getId());
  }

  @Test
  void unassignedTeacher_cannotCreateGrade() {
    assertThatThrownBy(
            () ->
                gradeService.create(
                    new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("10.00")),
                    otherTeacherUser))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("not assigned");
  }

  @Test
  void cannotCreateDuplicateGrade_forSameStudentAndExam() {
    gradeService.create(
        new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("11.00")), adminUser);

    assertThatThrownBy(
            () ->
                gradeService.create(
                    new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("13.00")),
                    adminUser))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("already exists");
  }

  // =========================================================================
  // 2. Historisation (§9)
  // =========================================================================

  @Test
  void update_preservesHistory() {
    GradeResponse created =
        gradeService.create(
            new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("8.50")), adminUser);

    GradeResponse updated =
        gradeService.update(
            created.id(),
            new GradeUpdateRequest(new BigDecimal("10.50"), "Correction après réclamation"),
            adminUser);

    assertThat(updated.value()).isEqualByComparingTo("10.50");

    List<GradeHistoryResponse> history = gradeService.getHistory(created.id());
    assertThat(history).hasSize(1);
    assertThat(history.get(0).oldValue()).isEqualByComparingTo("8.50");
    assertThat(history.get(0).newValue()).isEqualByComparingTo("10.50");
    assertThat(history.get(0).reason()).isEqualTo("Correction après réclamation");
    assertThat(history.get(0).changedByUserId()).isEqualTo(adminUser.getId());
    assertThat(history.get(0).changedAt()).isNotNull();
  }

  @Test
  void unassignedTeacher_cannotUpdateGrade() {
    GradeResponse created =
        gradeService.create(
            new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("12.00")),
            adminUser);

    assertThatThrownBy(
            () ->
                gradeService.update(
                    created.id(),
                    new GradeUpdateRequest(new BigDecimal("15.00"), "Tentative illégale"),
                    otherTeacherUser))
        .isInstanceOf(AccessDeniedException.class);
  }

  // =========================================================================
  // 3. Publication (§7 / §10)
  // =========================================================================

  @Test
  void publish_fails_whenCoefficientSumNotExactlyOne() {
    // On ne crée qu'UN examen (coef 1/2) → somme ≠ 1
    // Pour ce test isolé : supprimer les 2 autres examens
    examRepository.delete(examQuarter1);
    examRepository.delete(examQuarter2);

    GradeResponse created =
        gradeService.create(
            new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("14.00")),
            adminUser);

    assertThatThrownBy(() -> gradeService.publish(created.id()))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("must equal exactly 1");
  }

  @Test
  void publish_succeeds_whenCoefficientSumEqualsOne() {
    GradeResponse created =
        gradeService.create(
            new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("16.00")),
            adminUser);

    // Les 3 examens existent déjà (1/2 + 1/4 + 1/4 = 1)
    GradeResponse published = gradeService.publish(created.id());

    assertThat(published.status()).isEqualTo(GradeStatus.PUBLISHED);
  }

  // =========================================================================
  // 4. Note finale pondérée (§7 / §8)
  // =========================================================================

  @Test
  void getFinalGrade_computesWeightedAverage() {
    // Partiel 1/2 → 16  |  TP1 1/4 → 12  |  TP2 1/4 → 8
    // final = 16*(1/2) + 12*(1/4) + 8*(1/4) = 8 + 3 + 2 = 13.00
    createAndPublish(examHalf, "16.00");
    createAndPublish(examQuarter1, "12.00");
    createAndPublish(examQuarter2, "8.00");

    Optional<BigDecimal> finalGrade = gradeService.getFinalGrade(student.getId(), course.getId());

    assertThat(finalGrade).isPresent();
    assertThat(finalGrade.get()).isEqualByComparingTo("13.00");
  }

  @Test
  void getFinalGrade_returnsEmpty_whenNoPublishedGrades() {
    // Notes en DRAFT uniquement
    gradeService.create(
        new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("15.00")), adminUser);

    Optional<BigDecimal> finalGrade = gradeService.getFinalGrade(student.getId(), course.getId());

    assertThat(finalGrade).isEmpty();
  }

  // =========================================================================
  // 5. Visibilité étudiant (§6 / §18)
  // =========================================================================

  @Test
  void student_seesOnlyPublishedGrades() {
    GradeResponse draft =
        gradeService.create(
            new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("11.00")),
            adminUser);
    GradeResponse toPublish =
        gradeService.create(
            new GradeRequest(student.getId(), examQuarter1.getId(), new BigDecimal("13.00")),
            adminUser);
    gradeService.publish(toPublish.id());

    List<GradeResponse> myGrades = gradeService.findMyPublishedGrades(studentUser.getId());

    assertThat(myGrades).hasSize(1);
    assertThat(myGrades.get(0).id()).isEqualTo(toPublish.id());
    assertThat(myGrades.get(0).status()).isEqualTo(GradeStatus.PUBLISHED);
    assertThat(myGrades).noneMatch(g -> g.id().equals(draft.id()));
  }

  @Test
  void assignedTeacher_seesStudentGrades_forOwnCourse() {
    gradeService.create(
        new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("14.00")), adminUser);

    List<GradeResponse> grades = gradeService.findGradesForStudent(student.getId(), teacherUser);

    assertThat(grades).hasSize(1);
    assertThat(grades.get(0).courseId()).isEqualTo(course.getId());
  }

  @Test
  void unassignedTeacher_seesNoGrades_forStudent() {
    gradeService.create(
        new GradeRequest(student.getId(), examHalf.getId(), new BigDecimal("14.00")), adminUser);

    List<GradeResponse> grades =
        gradeService.findGradesForStudent(student.getId(), otherTeacherUser);

    assertThat(grades).isEmpty();
  }

  // ---------- helpers ----------

  private void createAndPublish(Exam exam, String value) {
    GradeResponse created =
        gradeService.create(
            new GradeRequest(student.getId(), exam.getId(), new BigDecimal(value)), adminUser);
    gradeService.publish(created.id());
  }
}
