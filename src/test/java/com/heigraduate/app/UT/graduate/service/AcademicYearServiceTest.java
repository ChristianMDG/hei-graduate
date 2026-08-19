package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.dto.AcademicYearRequest;
import com.heigraduate.app.graduate.exception.BadRequestException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.service.AcademicYearService;
import com.heigraduate.app.graduate.validator.AcademicYearValidator;
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
class AcademicYearServiceTest {

  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private AcademicYearValidator academicYearValidator;

  @InjectMocks private AcademicYearService academicYearService;

  private AcademicYear academicYear;
  private UUID yearId;

  @BeforeEach
  void setUp() {
    yearId = UUID.randomUUID();
    academicYear =
        AcademicYear.builder()
            .id(yearId)
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 6, 30))
            .level("L1")
            .build();
  }

  @Test
  void create_shouldPersistAcademicYear_whenDatesAreValid() {
    AcademicYearRequest request =
        new AcademicYearRequest(
            "2025-2026", LocalDate.of(2025, 9, 1), LocalDate.of(2026, 6, 30), "L1");

    when(academicYearRepository.save(any(AcademicYear.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AcademicYear result = academicYearService.create(request);

    assertThat(result.getLabel()).isEqualTo("2025-2026");
    assertThat(result.getLevel()).isEqualTo("L1");
    assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 9, 1));
    verify(academicYearRepository).save(any(AcademicYear.class));
    verify(academicYearValidator)
        .validateDateRange(LocalDate.of(2025, 9, 1), LocalDate.of(2026, 6, 30));
  }

  @Test
  void create_shouldThrowBadRequest_whenStartDateAfterEndDate() {
    AcademicYearRequest request =
        new AcademicYearRequest(
            "2025-2026", LocalDate.of(2026, 6, 30), LocalDate.of(2025, 9, 1), "L1");

    doThrow(new BadRequestException("startDate must be before endDate"))
        .when(academicYearValidator)
        .validateDateRange(LocalDate.of(2026, 6, 30), LocalDate.of(2025, 9, 1));

    assertThatThrownBy(() -> academicYearService.create(request))
        .isInstanceOf(BadRequestException.class);

    verify(academicYearRepository, never()).save(any());
  }

  @Test
  void update_shouldModifyExistingAcademicYear() {
    AcademicYearRequest request =
        new AcademicYearRequest(
            "2026-2027", LocalDate.of(2026, 9, 1), LocalDate.of(2027, 6, 30), "L2");

    when(academicYearRepository.findById(yearId)).thenReturn(Optional.of(academicYear));
    when(academicYearRepository.save(any(AcademicYear.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    AcademicYear result = academicYearService.update(yearId, request);

    assertThat(result.getLabel()).isEqualTo("2026-2027");
    assertThat(result.getLevel()).isEqualTo("L2");
    verify(academicYearRepository).save(any(AcademicYear.class));
  }

  @Test
  void update_shouldThrow_whenAcademicYearNotFound() {
    UUID unknownId = UUID.randomUUID();
    AcademicYearRequest request =
        new AcademicYearRequest(
            "2025-2026", LocalDate.of(2025, 9, 1), LocalDate.of(2026, 6, 30), "L1");

    when(academicYearRepository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> academicYearService.update(unknownId, request))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(academicYearRepository, never()).save(any());
  }

  @Test
  void getById_shouldReturnAcademicYear_whenExists() {
    when(academicYearRepository.findById(yearId)).thenReturn(Optional.of(academicYear));

    AcademicYear result = academicYearService.getById(yearId);

    assertThat(result.getId()).isEqualTo(yearId);
    assertThat(result.getLabel()).isEqualTo("2025-2026");
  }

  @Test
  void getById_shouldThrow_whenNotFound() {
    UUID unknownId = UUID.randomUUID();
    when(academicYearRepository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> academicYearService.getById(unknownId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getAll_shouldReturnAllAcademicYears() {
    when(academicYearRepository.findAll()).thenReturn(List.of(academicYear));

    List<AcademicYear> result = academicYearService.getAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getLabel()).isEqualTo("2025-2026");
  }
}
