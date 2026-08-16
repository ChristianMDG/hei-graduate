package com.heigraduate.app.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.heigraduate.app.graduate.dto.SemesterRequest;
import com.heigraduate.app.graduate.dto.SemesterResponse;
import com.heigraduate.app.graduate.exception.BadRequestException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.Semester;
import com.heigraduate.app.graduate.repository.SemesterRepository;
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
class SemesterServiceTest {

  @Mock private SemesterRepository semesterRepository;

  @InjectMocks private SemesterService semesterService;

  private Semester semester;
  private UUID semesterId;

  @BeforeEach
  void setUp() {
    semesterId = UUID.randomUUID();
    semester =
        Semester.builder()
            .id(semesterId)
            .label("Semestre 1")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 1, 31))
            .expectedCredits(30)
            .active(true)
            .build();
  }

  @Test
  void findAll_shouldReturnAllSemesters() {
    when(semesterRepository.findAll()).thenReturn(List.of(semester));

    List<SemesterResponse> result = semesterService.findAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).label()).isEqualTo("Semestre 1");
  }

  @Test
  void findById_shouldReturnSemester_whenExists() {
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));

    SemesterResponse result = semesterService.findById(semesterId);

    assertThat(result.id()).isEqualTo(semesterId);
  }

  @Test
  void findById_shouldThrow_whenNotFound() {
    UUID unknownId = UUID.randomUUID();
    when(semesterRepository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> semesterService.findById(unknownId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void create_shouldSaveSemester_whenDatesAreValid() {
    SemesterRequest request =
        new SemesterRequest("Semestre 2", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), 30);
    when(semesterRepository.save(any(Semester.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SemesterResponse result = semesterService.create(request);

    assertThat(result.label()).isEqualTo("Semestre 2");
    assertThat(result.expectedCredits()).isEqualTo(30);
    verify(semesterRepository).save(any(Semester.class));
  }

  @Test
  void create_shouldThrowBadRequest_whenStartDateAfterEndDate() {
    SemesterRequest request =
        new SemesterRequest("Semestre X", LocalDate.of(2026, 6, 30), LocalDate.of(2026, 2, 1), 30);

    assertThatThrownBy(() -> semesterService.create(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("startDate must be before endDate");

    verify(semesterRepository, never()).save(any());
  }

  @Test
  void create_shouldThrowBadRequest_whenStartDateEqualsEndDate() {
    LocalDate sameDate = LocalDate.of(2026, 3, 1);
    SemesterRequest request = new SemesterRequest("Semestre X", sameDate, sameDate, 30);

    assertThatThrownBy(() -> semesterService.create(request))
        .isInstanceOf(BadRequestException.class);

    verify(semesterRepository, never()).save(any());
  }

  @Test
  void update_shouldModifyExistingSemester() {
    SemesterRequest request =
        new SemesterRequest(
            "Semestre 1 - modifié", LocalDate.of(2025, 9, 15), LocalDate.of(2026, 2, 15), 30);
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));
    when(semesterRepository.save(any(Semester.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SemesterResponse result = semesterService.update(semesterId, request);

    assertThat(result.label()).isEqualTo("Semestre 1 - modifié");
  }

  @Test
  void update_shouldThrow_whenSemesterNotFound() {
    UUID unknownId = UUID.randomUUID();
    SemesterRequest request =
        new SemesterRequest("X", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), 30);
    when(semesterRepository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> semesterService.update(unknownId, request))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void deactivate_shouldSetActiveToFalse() {
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));
    when(semesterRepository.save(any(Semester.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    semesterService.deactivate(semesterId);

    assertThat(semester.getActive()).isFalse();
  }
}
