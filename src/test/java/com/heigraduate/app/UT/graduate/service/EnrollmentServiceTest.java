package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.dto.EnrollmentRequest;
import com.heigraduate.app.graduate.dto.TransferRequest;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.repository.EnrollmentRepository;
import com.heigraduate.app.graduate.service.EnrollmentService;
import com.heigraduate.app.graduate.validator.EnrollmentValidator;
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
class EnrollmentServiceTest {

  @Mock private EnrollmentRepository enrollmentRepository;
  @Mock private EnrollmentValidator enrollmentValidator;

  @InjectMocks private EnrollmentService enrollmentService;

  private UUID studentId;
  private UUID parcoursId;
  private UUID groupK1;
  private UUID groupK2;
  private UUID groupK3;

  @BeforeEach
  void setUp() {
    studentId = UUID.randomUUID();
    parcoursId = UUID.randomUUID();
    groupK1 = UUID.randomUUID();
    groupK2 = UUID.randomUUID();
    groupK3 = UUID.randomUUID();
  }

  @Test
  void enroll_shouldCreateNewEnrollment_whenNoActiveEnrollmentExists() {
    EnrollmentRequest request =
        new EnrollmentRequest(studentId, parcoursId, groupK3, LocalDate.of(2025, 9, 1));

    Enrollment expected =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursId)
            .groupId(groupK3)
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(null)
            .build();

    when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(expected);

    Enrollment result = enrollmentService.enroll(request);

    assertThat(result.getGroupId()).isEqualTo(groupK3);
    assertThat(result.getEndDate()).isNull();
    verify(enrollmentValidator).validateNoActiveEnrollment(studentId);
    verify(enrollmentRepository).save(any(Enrollment.class));
  }

  @Test
  void enroll_shouldThrowConflict_whenActiveEnrollmentAlreadyExists() {
    EnrollmentRequest request =
        new EnrollmentRequest(studentId, parcoursId, groupK3, LocalDate.of(2025, 9, 1));

    doThrow(
            new ConflictException(
                "Student already has an active enrollment — use the transfer endpoint instead"))
        .when(enrollmentValidator)
        .validateNoActiveEnrollment(studentId);

    assertThatThrownBy(() -> enrollmentService.enroll(request))
        .isInstanceOf(ConflictException.class);

    verify(enrollmentRepository, times(0)).save(any());
  }

  @Test
  void transfer_shouldChangeGroupAndPreserveHistory_example_K3_to_K1() {
    LocalDate startDate1 = LocalDate.of(2025, 9, 1);
    LocalDate transferDate1 = LocalDate.of(2026, 2, 1);

    Enrollment enrollment1 =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursId)
            .groupId(groupK3)
            .startDate(startDate1)
            .endDate(null)
            .build();

    TransferRequest transfer1 = new TransferRequest(null, groupK1, transferDate1);

    Enrollment enrollment2 =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursId)
            .groupId(groupK1)
            .startDate(transferDate1)
            .endDate(null)
            .build();

    when(enrollmentRepository.findByStudentIdAndEndDateIsNull(studentId))
        .thenReturn(Optional.of(enrollment1));

    when(enrollmentRepository.save(any(Enrollment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Enrollment result = enrollmentService.transfer(studentId, transfer1);

    assertThat(result.getGroupId()).isEqualTo(groupK1);
    assertThat(result.getStartDate()).isEqualTo(transferDate1);
    assertThat(result.getEndDate()).isNull();
    assertThat(enrollment1.getEndDate()).isEqualTo(transferDate1);
    verify(enrollmentValidator).validateTransferDate(enrollment1, transferDate1);
  }

  @Test
  void transfer_shouldThrowConflict_whenEffectiveDateBeforeStartDate() {
    Enrollment enrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursId)
            .groupId(groupK3)
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(null)
            .build();

    TransferRequest invalidTransfer = new TransferRequest(null, groupK1, LocalDate.of(2025, 8, 1));

    when(enrollmentRepository.findByStudentIdAndEndDateIsNull(studentId))
        .thenReturn(Optional.of(enrollment));

    doThrow(
            new ConflictException(
                "effectiveDate must be after the current enrollment's start date"))
        .when(enrollmentValidator)
        .validateTransferDate(enrollment, LocalDate.of(2025, 8, 1));

    assertThatThrownBy(() -> enrollmentService.transfer(studentId, invalidTransfer))
        .isInstanceOf(ConflictException.class);
  }

  @Test
  void getCurrent_shouldReturnActiveEnrollment() {
    Enrollment activeEnrollment =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursId)
            .groupId(groupK1)
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(null)
            .build();

    when(enrollmentRepository.findByStudentIdAndEndDateIsNull(studentId))
        .thenReturn(Optional.of(activeEnrollment));

    Enrollment result = enrollmentService.getCurrent(studentId);

    assertThat(result.getEndDate()).isNull();
    assertThat(result.getGroupId()).isEqualTo(groupK1);
  }

  @Test
  void getCurrent_shouldThrow_whenNoActiveEnrollment() {
    when(enrollmentRepository.findByStudentIdAndEndDateIsNull(studentId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> enrollmentService.getCurrent(studentId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getHistory_shouldReturnAllEnrollmentsInChronologicalOrder() {
    Enrollment enrollment1 =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursId)
            .groupId(groupK3)
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 2, 1))
            .build();

    Enrollment enrollment2 =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursId)
            .groupId(groupK1)
            .startDate(LocalDate.of(2026, 2, 1))
            .endDate(null)
            .build();

    when(enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId))
        .thenReturn(List.of(enrollment1, enrollment2));

    List<Enrollment> result = enrollmentService.getHistory(studentId);

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getGroupId()).isEqualTo(groupK3);
    assertThat(result.get(1).getGroupId()).isEqualTo(groupK1);
  }

  @Test
  void getActiveAt_shouldReturnEnrollmentAtSpecificDate() {
    LocalDate queryDate = LocalDate.of(2026, 1, 15);
    Enrollment enrollmentOnQueryDate =
        Enrollment.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .parcoursId(parcoursId)
            .groupId(groupK3)
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 2, 1))
            .build();

    when(enrollmentRepository.findActiveAt(studentId, queryDate))
        .thenReturn(Optional.of(enrollmentOnQueryDate));

    Enrollment result = enrollmentService.getActiveAt(studentId, queryDate);

    assertThat(result.getGroupId()).isEqualTo(groupK3);
  }
}
