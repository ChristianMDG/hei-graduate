package com.heigraduate.app.graduate.validator;

import com.heigraduate.app.graduate.exception.BadRequestException;
import com.heigraduate.app.graduate.model.Exam;
import com.heigraduate.app.graduate.repository.ExamRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExamValidator {

  private final ExamRepository examRepository;

  public void validateTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {
    if (!startTime.isBefore(endTime)) {
      throw new BadRequestException("startTime must be before endTime");
    }
  }

  public void validateCoefficientFraction(Integer numerator, Integer denominator) {
    if (denominator == 0) {
      throw new BadRequestException("coefficientDenominator must not be zero");
    }
    if (numerator > denominator) {
      throw new BadRequestException("coefficientNumerator must not exceed coefficientDenominator");
    }
  }

  public void validateCoefficientSum(
      UUID courseId,
      UUID semesterId,
      Integer newNumerator,
      Integer newDenominator,
      UUID excludeExamId) {
    List<Exam> existingExams = examRepository.findByCourseIdAndSemesterId(courseId, semesterId);

    long sumNumerator = newNumerator;
    long sumDenominator = newDenominator;

    for (Exam exam : existingExams) {
      if (excludeExamId != null && exam.getId().equals(excludeExamId)) {
        continue;
      }
      long crossNumerator =
          sumNumerator * exam.getCoefficientDenominator()
              + exam.getCoefficientNumerator() * sumDenominator;
      long crossDenominator = sumDenominator * (long) exam.getCoefficientDenominator();

      sumNumerator = crossNumerator;
      sumDenominator = crossDenominator;
    }

    if (sumNumerator > sumDenominator) {
      throw new BadRequestException(
          "Sum of coefficients for this course/semester would exceed 1 (currently: "
              + sumNumerator
              + "/"
              + sumDenominator
              + ")");
    }
  }

  public void validateCoefficientSumEqualsOne(UUID courseId, UUID semesterId) {
    List<Exam> exams = examRepository.findByCourseIdAndSemesterId(courseId, semesterId);

    if (exams.isEmpty()) {
      throw new BadRequestException(
          "No exams found for course " + courseId + " in semester " + semesterId);
    }

    long sumNumerator = 0;
    long sumDenominator = 1;

    for (Exam exam : exams) {

      long crossNumerator =
          sumNumerator * exam.getCoefficientDenominator()
              + exam.getCoefficientNumerator() * sumDenominator;
      long crossDenominator = sumDenominator * (long) exam.getCoefficientDenominator();
      sumNumerator = crossNumerator;
      sumDenominator = crossDenominator;
    }

    if (sumNumerator != sumDenominator) {
      throw new BadRequestException(
          "Sum of coefficients for course "
              + courseId
              + " in semester "
              + semesterId
              + " must equal exactly 1, but is "
              + sumNumerator
              + "/"
              + sumDenominator
              + ". All exams must be created before publishing grades.");
    }
  }
}
