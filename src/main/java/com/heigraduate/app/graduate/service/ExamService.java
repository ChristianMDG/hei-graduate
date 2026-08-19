package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.ExamRequest;
import com.heigraduate.app.graduate.dto.ExamResponse;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.mapper.ExamMapper;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.Exam;
import com.heigraduate.app.graduate.model.Semester;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.ExamRepository;
import com.heigraduate.app.graduate.repository.SemesterRepository;
import com.heigraduate.app.graduate.validator.ExamValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExamService {

  private final ExamRepository examRepository;
  private final CourseRepository courseRepository;
  private final AcademicYearRepository academicYearRepository;
  private final SemesterRepository semesterRepository;
  private final ExamValidator examValidator;

  @Transactional(readOnly = true)
  public List<ExamResponse> findAll() {
    return examRepository.findAll().stream().map(ExamMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ExamResponse findById(UUID id) {
    return examRepository
        .findById(id)
        .map(ExamMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));
  }

  @Transactional
  public ExamResponse create(ExamRequest request) {
    Course course =
        courseRepository
            .findById(request.courseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Course not found with id: " + request.courseId()));

    AcademicYear academicYear =
        academicYearRepository
            .findById(request.academicYearId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "AcademicYear not found with id: " + request.academicYearId()));

    Semester semester =
        semesterRepository
            .findById(request.semesterId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Semester not found with id: " + request.semesterId()));

    examValidator.validateTimeRange(request.startTime(), request.endTime());

    examValidator.validateCoefficientFraction(
        request.coefficientNumerator(), request.coefficientDenominator());

    examValidator.validateCoefficientSum(
        request.courseId(),
        request.semesterId(),
        request.coefficientNumerator(),
        request.coefficientDenominator(),
        null);

    Exam exam =
        Exam.builder()
            .course(course)
            .academicYear(academicYear)
            .semester(semester)
            .label(request.label())
            .date(request.date())
            .startTime(request.startTime())
            .endTime(request.endTime())
            .coefficientNumerator(request.coefficientNumerator())
            .coefficientDenominator(request.coefficientDenominator())
            .build();

    return ExamMapper.toResponse(examRepository.save(exam));
  }

  @Transactional
  public ExamResponse update(UUID id, ExamRequest request) {
    Exam exam =
        examRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exam not found with id: " + id));

    Semester semester =
        semesterRepository
            .findById(request.semesterId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Semester not found with id: " + request.semesterId()));

    examValidator.validateTimeRange(request.startTime(), request.endTime());

    examValidator.validateCoefficientFraction(
        request.coefficientNumerator(), request.coefficientDenominator());

    examValidator.validateCoefficientSum(
        request.courseId(),
        request.semesterId(),
        request.coefficientNumerator(),
        request.coefficientDenominator(),
        id);

    exam.setSemester(semester);
    exam.setLabel(request.label());
    exam.setDate(request.date());
    exam.setStartTime(request.startTime());
    exam.setEndTime(request.endTime());
    exam.setCoefficientNumerator(request.coefficientNumerator());
    exam.setCoefficientDenominator(request.coefficientDenominator());

    return ExamMapper.toResponse(examRepository.save(exam));
  }

  @Transactional
  public void delete(UUID id) {
    if (!examRepository.existsById(id)) {
      throw new ResourceNotFoundException("Exam not found with id: " + id);
    }

    examRepository.deleteById(id);
  }
}
