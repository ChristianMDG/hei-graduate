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

  /**
   * BUG-01 FIX — Valide que la somme des coefficients du cours pour ce semestre ne dépasse pas 1.
   *
   * <p>La somme est calculée par (cours, semestre) et non par (cours, année) — un même cours peut
   * figurer en S1 et en S2 via COURS_PARCOURS ; agréger sur toute l'année mélangeait les deux pools
   * et rejetait des configurations valides.
   *
   * @param excludeExamId UUID de l'examen à exclure du cumul (cas de la mise à jour, null à la
   *     création).
   */
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

  /**
   * BUG-10 FIX — Vérifie que la somme des coefficients est EXACTEMENT 1 (§7 cahier des charges).
   *
   * <p>"La somme des coefficients des examens d'un même cours doit être égale à 1."
   *
   * <p>Cette vérification complémentaire à {@link #validateCoefficientSum} doit être appelée
   * <b>avant la publication des notes</b> d'un cours donné pour un semestre donné : à ce moment,
   * tous les examens du cours doivent avoir été créés et leur somme doit être exactement 1. Si la
   * somme est inférieure à 1, la publication est bloquée.
   */
  public void validateCoefficientSumEqualsOne(UUID courseId, UUID semesterId) {
    List<Exam> exams = examRepository.findByCourseIdAndSemesterId(courseId, semesterId);

    if (exams.isEmpty()) {
      throw new BadRequestException(
          "No exams found for course " + courseId + " in semester " + semesterId);
    }

    long sumNumerator = 0;
    long sumDenominator = 1;

    for (Exam exam : exams) {
      // Addition de fractions : a/b + c/d = (a*d + c*b) / (b*d)
      long crossNumerator =
          sumNumerator * exam.getCoefficientDenominator()
              + exam.getCoefficientNumerator() * sumDenominator;
      long crossDenominator = sumDenominator * (long) exam.getCoefficientDenominator();
      sumNumerator = crossNumerator;
      sumDenominator = crossDenominator;
    }

    // Vérification : somme == 1 ⟺ numérateur == dénominateur
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
