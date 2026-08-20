package com.heigraduate.app.IT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.heigraduate.app.conf.FacadeIT;
import com.heigraduate.app.file.bucket.BucketComponent;
import com.heigraduate.app.file.hash.FileHash;
import com.heigraduate.app.file.hash.FileHashAlgorithm;
import com.heigraduate.app.graduate.model.*;
import com.heigraduate.app.graduate.repository.*;
import com.heigraduate.app.graduate.service.TranscriptService;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class TranscriptIT extends FacadeIT {

  @Autowired private TranscriptService transcriptService;

  @MockBean private BucketComponent bucketComponent;

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
  @Autowired private TranscriptRepository transcriptRepository;

  private AcademicYear academicYear;
  private Semester semester;
  private Parcours parcoursEl;
  private Group group;
  private Student student;
  private Course course5;
  private Course course3;

  @BeforeEach
  void setUp() {
    when(bucketComponent.upload(any(File.class), anyString()))
        .thenReturn(new FileHash(FileHashAlgorithm.SHA256, "dummy"));

    String uid = UUID.randomUUID().toString().substring(0, 6);

    parcoursEl =
        parcoursRepository.save(
            Parcours.builder().code("EL-" + uid).label("Écosystème Logiciel").active(true).build());
    group =
        groupRepository.save(
            Group.builder().reference("K1-" + uid).capacity(30).active(true).build());

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

    course5 = saveCourse("PROG4-" + uid, "Programmation", 5);
    course3 = saveCourse("WEB1-" + uid, "Web", 3);
    saveTrack(course5);
    saveTrack(course3);

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

    enrollmentRepository.save(
        Enrollment.builder()
            .studentId(student.getId())
            .parcoursId(parcoursEl.getId())
            .groupId(group.getId())
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(null)
            .build());
  }

  @Test
  void generateTranscript_complete_whenAllGradesPresent() {
    publishFinalGrade(course5, "16.00");
    publishFinalGrade(course3, "12.00");

    byte[] pdf = transcriptService.generateTranscript(student.getId(), academicYear.getId());

    assertThat(pdf).isNotEmpty();
    assertThat(new String(pdf)).contains("PDF"); // en-tête PDF
    assertThat(transcriptRepository.findAll()).hasSize(1);
    assertThat(transcriptRepository.findAll().get(0).getType()).isEqualTo("COMPLET");
  }

  @Test
  void generateTranscript_provisional_whenGradeMissing() {
    publishFinalGrade(course5, "14.00");
    // course3 sans note

    byte[] pdf = transcriptService.generateTranscript(student.getId(), academicYear.getId());

    assertThat(pdf).isNotEmpty();
    assertThat(transcriptRepository.findAll()).hasSize(1);
    assertThat(transcriptRepository.findAll().get(0).getType()).isEqualTo("PROVISOIRE");
  }

  private Course saveCourse(String ref, String title, int credits) {
    return courseRepository.save(
        Course.builder().courseReference(ref).title(title).credits(credits).active(true).build());
  }

  private void saveTrack(Course course) {
    courseTrackRepository.save(
        CourseTrack.builder()
            .course(course)
            .track(parcoursEl)
            .semester(semester)
            .mandatory(true)
            .build());
  }

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
