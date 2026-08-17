package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.heigraduate.app.graduate.dto.SemesterRequest;
import com.heigraduate.app.graduate.dto.SemesterResponse;
import com.heigraduate.app.graduate.exception.BadRequestException;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Semester;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.SemesterRepository;
import com.heigraduate.app.graduate.service.SemesterService;
import com.heigraduate.app.graduate.validator.SemesterValidator;
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
  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private SemesterValidator semesterValidator;

  @InjectMocks private SemesterService semesterService;

  private Semester semester;
  private UUID semesterId;
  private AcademicYear academicYear;
  private UUID academicYearId;

  @BeforeEach
  void setUp() {
    academicYearId = UUID.randomUUID();
    academicYear =
        AcademicYear.builder()
            .id(academicYearId)
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 6, 30))
            .level("L1")
            .build();

    semesterId = UUID.randomUUID();
    semester =
        Semester.builder()
            .id(semesterId)
            .academicYear(academicYear)
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
    assertThat(result.get(0).academicYearId()).isEqualTo(academicYearId);
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
  void create_shouldSaveSemester_whenValidationPasses() {
    SemesterRequest request =
        new SemesterRequest(
            academicYearId, "Semestre 2", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), 30);
    when(academicYearRepository.findById(academicYearId)).thenReturn(Optional.of(academicYear));
    when(semesterRepository.save(any(Semester.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SemesterResponse result = semesterService.create(request);

    assertThat(result.label()).isEqualTo("Semestre 2");
    assertThat(result.academicYearId()).isEqualTo(academicYearId);
    verify(semesterValidator).validateDateRange(request.startDate(), request.endDate());
    verify(semesterValidator).validateNoOverlap(request.startDate(), request.endDate(), null);
    verify(semesterValidator)
        .validateWithinAcademicYear(academicYear, request.startDate(), request.endDate());
    verify(semesterRepository).save(any(Semester.class));
  }

  @Test
  void create_shouldPropagateBadRequest_whenDateRangeInvalid() {
    SemesterRequest request =
        new SemesterRequest(
            academicYearId, "Semestre X", LocalDate.of(2026, 6, 30), LocalDate.of(2026, 2, 1), 30);
    doThrow(new BadRequestException("startDate must be before endDate"))
        .when(semesterValidator)
        .validateDateRange(request.startDate(), request.endDate());

    assertThatThrownBy(() -> semesterService.create(request))
        .isInstanceOf(BadRequestException.class);

    verify(semesterRepository, never()).save(any());
  }

  @Test
  void create_shouldPropagateConflict_whenDateRangeOverlapsExistingSemester() {
    SemesterRequest request =
        new SemesterRequest(
            academicYearId,
            "S1 2026-2027",
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2027, 1, 31),
            30);
    doThrow(
            new ConflictException(
                "This date range overlaps with an existing semester: S1 2026-2027"))
        .when(semesterValidator)
        .validateNoOverlap(request.startDate(), request.endDate(), null);

    assertThatThrownBy(() -> semesterService.create(request)).isInstanceOf(ConflictException.class);

    verify(semesterRepository, never()).save(any());
  }

  @Test
  void create_shouldThrow_whenAcademicYearNotFound() {
    UUID unknownYearId = UUID.randomUUID();
    SemesterRequest request =
        new SemesterRequest(
            unknownYearId, "Semestre 2", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), 30);
    when(academicYearRepository.findById(unknownYearId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> semesterService.create(request))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(semesterRepository, never()).save(any());
  }

  @Test
  void update_shouldModifyExistingSemester_whenValidationPasses() {
    SemesterRequest request =
        new SemesterRequest(
            academicYearId,
            "Semestre 1 - modifié",
            LocalDate.of(2025, 9, 15),
            LocalDate.of(2026, 2, 15),
            30);
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));
    when(academicYearRepository.findById(academicYearId)).thenReturn(Optional.of(academicYear));
    when(semesterRepository.save(any(Semester.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SemesterResponse result = semesterService.update(semesterId, request);

    assertThat(result.label()).isEqualTo("Semestre 1 - modifié");
    verify(semesterValidator).validateNoOverlap(request.startDate(), request.endDate(), semesterId);
  }

  @Test
  void update_shouldExcludeItselfFromOverlapCheck() {
    SemesterRequest request =
        new SemesterRequest(
            academicYearId, "Semestre 1", LocalDate.of(2025, 9, 1), LocalDate.of(2026, 1, 31), 30);
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));
    when(academicYearRepository.findById(academicYearId)).thenReturn(Optional.of(academicYear));
    when(semesterRepository.save(any(Semester.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    semesterService.update(semesterId, request);

    verify(semesterValidator).validateNoOverlap(request.startDate(), request.endDate(), semesterId);
  }

  @Test
  void update_shouldThrow_whenSemesterNotFound() {
    UUID unknownId = UUID.randomUUID();
    SemesterRequest request =
        new SemesterRequest(
            academicYearId, "X", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1), 30);
    when(semesterRepository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> semesterService.update(unknownId, request))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(semesterRepository, never()).save(any());
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
