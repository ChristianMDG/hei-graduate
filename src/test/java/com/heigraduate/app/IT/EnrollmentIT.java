package com.heigraduate.app.IT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.heigraduate.app.conf.FacadeIT;
import com.heigraduate.app.graduate.dto.EnrollmentRequest;
import com.heigraduate.app.graduate.dto.TransferRequest;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.*;
import com.heigraduate.app.graduate.repository.*;
import com.heigraduate.app.graduate.service.EnrollmentService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class EnrollmentIT extends FacadeIT {

  @Autowired private EnrollmentService enrollmentService;

  @Autowired private UserRepository userRepository;
  @Autowired private StudentRepository studentRepository;
  @Autowired private ParcoursRepository parcoursRepository;
  @Autowired private GroupRepository groupRepository;

  private Student student;
  private Parcours parcoursEl;
  private Parcours parcoursTn;
  private Group groupK1;
  private Group groupK2;
  private Group groupK3;

  @BeforeEach
  void setUp() {
    String uid = UUID.randomUUID().toString().substring(0, 6);

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
    groupK3 =
        groupRepository.save(
            Group.builder().reference("K3-" + uid).capacity(30).active(true).build());

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
                .enrollmentDate(LocalDate.of(2023, 9, 1))
                .status("ACTIVE")
                .build());
  }

  @Test
  void enroll_createsActiveEnrollment() {
    Enrollment enrollment =
        enrollmentService.enroll(
            new EnrollmentRequest(
                student.getId(), parcoursEl.getId(), groupK3.getId(), LocalDate.of(2023, 9, 1)));

    assertThat(enrollment.getId()).isNotNull();
    assertThat(enrollment.getEndDate()).isNull();
    assertThat(enrollment.getGroupId()).isEqualTo(groupK3.getId());
    assertThat(enrollment.getParcoursId()).isEqualTo(parcoursEl.getId());
  }

  @Test
  void transfer_preservesHistory_andCreatesNewActiveEnrollment() {
    enrollmentService.enroll(
        new EnrollmentRequest(
            student.getId(), parcoursEl.getId(), groupK3.getId(), LocalDate.of(2023, 9, 1)));

    Enrollment transferred =
        enrollmentService.transfer(
            student.getId(),
            new TransferRequest(parcoursEl.getId(), groupK1.getId(), LocalDate.of(2024, 9, 1)));

    assertThat(transferred.getGroupId()).isEqualTo(groupK1.getId());
    assertThat(transferred.getEndDate()).isNull();

    List<Enrollment> history = enrollmentService.getHistory(student.getId());
    assertThat(history).hasSize(2);
    assertThat(history.get(0).getEndDate()).isEqualTo(LocalDate.of(2024, 9, 1));
    assertThat(history.get(0).getGroupId()).isEqualTo(groupK3.getId());
    assertThat(history.get(1).getGroupId()).isEqualTo(groupK1.getId());
  }

  @Test
  void transfer_canChangeParcours() {
    enrollmentService.enroll(
        new EnrollmentRequest(
            student.getId(), parcoursTn.getId(), groupK2.getId(), LocalDate.of(2023, 9, 1)));

    Enrollment transferred =
        enrollmentService.transfer(
            student.getId(),
            new TransferRequest(parcoursEl.getId(), groupK1.getId(), LocalDate.of(2024, 7, 1)));

    assertThat(transferred.getParcoursId()).isEqualTo(parcoursEl.getId());
    assertThat(transferred.getGroupId()).isEqualTo(groupK1.getId());
  }

  @Test
  void getCurrent_returnsActiveEnrollment() {
    enrollmentService.enroll(
        new EnrollmentRequest(
            student.getId(), parcoursEl.getId(), groupK1.getId(), LocalDate.of(2023, 9, 1)));

    Enrollment current = enrollmentService.getCurrent(student.getId());
    assertThat(current.getEndDate()).isNull();
    assertThat(current.getGroupId()).isEqualTo(groupK1.getId());
  }

  @Test
  void getActiveAt_returnsEnrollmentValidOnGivenDate() {
    enrollmentService.enroll(
        new EnrollmentRequest(
            student.getId(), parcoursEl.getId(), groupK3.getId(), LocalDate.of(2023, 9, 1)));
    enrollmentService.transfer(
        student.getId(), new TransferRequest(null, groupK1.getId(), LocalDate.of(2024, 9, 1)));

    Enrollment atL1 = enrollmentService.getActiveAt(student.getId(), LocalDate.of(2024, 1, 15));
    assertThat(atL1.getGroupId()).isEqualTo(groupK3.getId());

    Enrollment atL2 = enrollmentService.getActiveAt(student.getId(), LocalDate.of(2025, 1, 15));
    assertThat(atL2.getGroupId()).isEqualTo(groupK1.getId());
  }

  @Test
  void getCurrent_throws_whenNoActiveEnrollment() {
    assertThatThrownBy(() -> enrollmentService.getCurrent(student.getId()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("No active enrollment");
  }

  @Test
  void multipleTransfers_keepFullHistory() {
    enrollmentService.enroll(
        new EnrollmentRequest(
            student.getId(), parcoursEl.getId(), groupK3.getId(), LocalDate.of(2023, 9, 1)));
    enrollmentService.transfer(
        student.getId(), new TransferRequest(null, groupK1.getId(), LocalDate.of(2024, 9, 1)));
    enrollmentService.transfer(
        student.getId(), new TransferRequest(null, groupK2.getId(), LocalDate.of(2025, 2, 1)));

    List<Enrollment> history = enrollmentService.getHistory(student.getId());
    assertThat(history).hasSize(3);
    assertThat(history.get(0).getGroupId()).isEqualTo(groupK3.getId());
    assertThat(history.get(1).getGroupId()).isEqualTo(groupK1.getId());
    assertThat(history.get(2).getGroupId()).isEqualTo(groupK2.getId());
    assertThat(history.get(2).getEndDate()).isNull();
  }
}
