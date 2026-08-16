package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.heigraduate.app.graduate.dto.ExamRequest;
import com.heigraduate.app.graduate.dto.ExamResponse;
import com.heigraduate.app.graduate.exception.BadRequestException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.Exam;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.ExamRepository;
import com.heigraduate.app.graduate.service.ExamService;
import java.math.BigDecimal;
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
class ExamServiceTest {

  @Mock private ExamRepository examRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private AcademicYearRepository academicYearRepository;

  @InjectMocks private ExamService examService;

  private Course course;
  private AcademicYear year;

  @BeforeEach
  void setUp() {
    course =
        Course.builder()
            .id(UUID.randomUUID())
            .courseReference("PROG4")
            .title("Programmation 4")
            .credits(5)
            .active(true)
            .build();

    year =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 6, 30))
            .level("L2")
            .build();
  }

  @Test
  void create_shouldSaveExam_whenCoefficientSumStaysUnderOne() {
    ExamRequest request =
        new ExamRequest(course.getId(), year.getId(), "Examen final", new BigDecimal("0.6"));

    when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
    when(academicYearRepository.findById(year.getId())).thenReturn(Optional.of(year));
    when(examRepository.findByCourseIdAndAcademicYearId(course.getId(), year.getId()))
        .thenReturn(List.of());
    when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ExamResponse result = examService.create(request);

    assertThat(result.coefficient()).isEqualByComparingTo("0.6");
    verify(examRepository).save(any(Exam.class));
  }

  @Test
  void create_shouldAccept_whenCoefficientSumIsExactlyOne() {
    Exam existingExam =
        Exam.builder()
            .id(UUID.randomUUID())
            .course(course)
            .academicYear(year)
            .label("Controle continu")
            .coefficient(new BigDecimal("0.6"))
            .build();

    ExamRequest request =
        new ExamRequest(course.getId(), year.getId(), "Examen final", new BigDecimal("0.4"));

    when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
    when(academicYearRepository.findById(year.getId())).thenReturn(Optional.of(year));
    when(examRepository.findByCourseIdAndAcademicYearId(course.getId(), year.getId()))
        .thenReturn(List.of(existingExam));
    when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ExamResponse result = examService.create(request);

    assertThat(result.coefficient()).isEqualByComparingTo("0.4");
    verify(examRepository).save(any(Exam.class));
  }

  @Test
  void create_shouldThrowBadRequest_whenCoefficientSumExceedsOne() {
    Exam existingExam =
        Exam.builder()
            .id(UUID.randomUUID())
            .course(course)
            .academicYear(year)
            .label("Controle continu")
            .coefficient(new BigDecimal("0.7"))
            .build();

    ExamRequest request =
        new ExamRequest(course.getId(), year.getId(), "Examen final", new BigDecimal("0.5"));

    when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));
    when(academicYearRepository.findById(year.getId())).thenReturn(Optional.of(year));
    when(examRepository.findByCourseIdAndAcademicYearId(course.getId(), year.getId()))
        .thenReturn(List.of(existingExam));

    assertThatThrownBy(() -> examService.create(request))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("exceed 1");

    verify(examRepository, never()).save(any());
  }

  @Test
  void update_shouldExcludeCurrentExamFromSum() {
    UUID examId = UUID.randomUUID();
    Exam examToUpdate =
        Exam.builder()
            .id(examId)
            .course(course)
            .academicYear(year)
            .label("Controle continu")
            .coefficient(new BigDecimal("0.3"))
            .build();

    ExamRequest request =
        new ExamRequest(course.getId(), year.getId(), "Controle continu", new BigDecimal("0.5"));

    when(examRepository.findById(examId)).thenReturn(Optional.of(examToUpdate));
    when(examRepository.findByCourseIdAndAcademicYearId(course.getId(), year.getId()))
        .thenReturn(List.of(examToUpdate));
    when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

    ExamResponse result = examService.update(examId, request);

    assertThat(result.coefficient()).isEqualByComparingTo("0.5");
    verify(examRepository).save(any(Exam.class));
  }

  @Test
  void create_shouldThrow_whenCourseNotFound() {
    ExamRequest request =
        new ExamRequest(course.getId(), year.getId(), "Examen final", new BigDecimal("0.5"));
    when(courseRepository.findById(course.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> examService.create(request))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void delete_shouldThrow_whenExamNotFound() {
    UUID unknownId = UUID.randomUUID();
    when(examRepository.existsById(unknownId)).thenReturn(false);

    assertThatThrownBy(() -> examService.delete(unknownId))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(examRepository, never()).deleteById(any());
  }
}
