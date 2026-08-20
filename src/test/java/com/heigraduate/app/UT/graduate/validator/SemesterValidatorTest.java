package com.heigraduate.app.UT.graduate.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.exception.BadRequestException;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.model.Semester;
import com.heigraduate.app.graduate.repository.SemesterRepository;
import com.heigraduate.app.graduate.validator.SemesterValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SemesterValidatorTest {

  @Mock private SemesterRepository semesterRepository;
  @InjectMocks private SemesterValidator semesterValidator;

  private static final LocalDate START = LocalDate.of(2023, 9, 1);
  private static final LocalDate END = LocalDate.of(2024, 1, 31);

  @Test
  void validateDateRange_shouldPass_whenStartBeforeEnd() {
    assertThatCode(() -> semesterValidator.validateDateRange(START, END))
        .doesNotThrowAnyException();
  }

  @Test
  void validateDateRange_shouldThrow_whenStartAfterEnd() {
    assertThatThrownBy(() -> semesterValidator.validateDateRange(END, START))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void validateNoOverlap_shouldPass_whenNothingOverlaps_andNoExclusion() {
    when(semesterRepository.findOverlapping(START, END)).thenReturn(List.of());

    assertThatCode(() -> semesterValidator.validateNoOverlap(START, END, null))
        .doesNotThrowAnyException();
    verify(semesterRepository, never()).findOverlappingExcluding(any(), any(), any());
  }

  @Test
  void validateNoOverlap_shouldThrow_whenAnotherSemesterOverlaps() {
    Semester overlapping = Semester.builder().id(UUID.randomUUID()).label("L1-S1").build();
    when(semesterRepository.findOverlapping(START, END)).thenReturn(List.of(overlapping));

    assertThatThrownBy(() -> semesterValidator.validateNoOverlap(START, END, null))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("L1-S1");
  }

  @Test
  void validateNoOverlap_shouldUseExcludingQuery_andExcludeItself_onUpdate() {
    UUID selfId = UUID.randomUUID();
    when(semesterRepository.findOverlappingExcluding(START, END, selfId)).thenReturn(List.of());

    assertThatCode(() -> semesterValidator.validateNoOverlap(START, END, selfId))
        .doesNotThrowAnyException();
    verify(semesterRepository, never()).findOverlapping(any(), any());
  }
}
