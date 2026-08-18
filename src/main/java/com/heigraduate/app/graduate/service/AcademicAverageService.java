package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.contract.FinalGradeQuery;
import com.heigraduate.app.graduate.dto.AnnualAverageResult;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.CourseTrack;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.CourseTrackRepository;
import com.heigraduate.app.graduate.repository.EnrollmentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcademicAverageService {

  private final EnrollmentRepository enrollmentRepository;
  private final CourseTrackRepository courseTrackRepository;
  private final CourseRepository courseRepository;
  private final AcademicYearRepository academicYearRepository;
  private final FinalGradeQuery finalGradeQuery;

  @Transactional(readOnly = true)
  public AnnualAverageResult computeAnnualAverage(UUID studentId, UUID academicYearId) {
    AcademicYear academicYear =
        academicYearRepository
            .findById(academicYearId)
            .orElseThrow(
                () -> new ResourceNotFoundException("AcademicYear not found: " + academicYearId));

    List<Enrollment> periodsDuringYear =
        enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId).stream()
            .filter(e -> overlaps(e, academicYear.getStartDate(), academicYear.getEndDate()))
            .toList();

    if (periodsDuringYear.isEmpty()) {
      throw new ResourceNotFoundException(
          "No enrollment found for student "
              + studentId
              + " during academic year "
              + academicYearId);
    }

    Set<UUID> parcoursIdsThisYear = new LinkedHashSet<>();
    for (Enrollment e : periodsDuringYear) {
      parcoursIdsThisYear.add(e.getParcoursId());
    }

    // BUG-07 FIX : un étudiant qui a changé de parcours dans la même année
    // (ex : common → EL) peut se retrouver avec plusieurs parcoursId dans parcoursIdsThisYear.
    // Si un cours est commun aux deux parcours, findByTrackIdAndAcademicYearId le retourne deux
    // fois. Le LinkedHashMap.put() garantit la déduplication par courseId : le deuxième put()
    // sur le même UUID écrase silencieusement le premier sans modifier la valeur (les crédits
    // sont les mêmes pour le même cours). Résultat : chaque cours est compté exactement une fois.
    Map<UUID, Integer> creditsByCourse = new LinkedHashMap<>();
    for (UUID parcoursId : parcoursIdsThisYear) {
      List<CourseTrack> tracks =
          courseTrackRepository.findByTrackIdAndAcademicYearId(parcoursId, academicYearId);
      for (CourseTrack ct : tracks) {
        Course course = ct.getCourse();
        // put() écrase les doublons — un cours commun à EL et TN n'est jamais compté deux fois.
        creditsByCourse.put(course.getId(), course.getCredits());
      }
    }

    BigDecimal weightedSum = BigDecimal.ZERO;
    int gradedCredits = 0;
    int obtainedCredits = 0;
    int expectedCredits = 0;
    List<UUID> validated = new ArrayList<>();
    List<UUID> notValidated = new ArrayList<>();
    List<UUID> missingGrade = new ArrayList<>();

    for (Map.Entry<UUID, Integer> entry : creditsByCourse.entrySet()) {
      UUID courseId = entry.getKey();
      int credits = entry.getValue();
      expectedCredits += credits;

      Optional<BigDecimal> finalGrade = finalGradeQuery.getFinalGrade(studentId, courseId);
      if (finalGrade.isEmpty()) {
        missingGrade.add(courseId);
        continue;
      }

      BigDecimal grade = finalGrade.get();
      weightedSum = weightedSum.add(grade.multiply(BigDecimal.valueOf(credits)));
      gradedCredits += credits;

      if (grade.compareTo(BigDecimal.TEN) >= 0) {
        validated.add(courseId);
        obtainedCredits += credits;
      } else {
        notValidated.add(courseId);
      }
    }

    BigDecimal average =
        gradedCredits == 0
            ? null
            : weightedSum.divide(BigDecimal.valueOf(gradedCredits), 2, RoundingMode.HALF_UP);

    return new AnnualAverageResult(
        studentId,
        academicYearId,
        average,
        obtainedCredits,
        expectedCredits,
        validated,
        notValidated,
        missingGrade);
  }

  private boolean overlaps(Enrollment enrollment, LocalDate yearStart, LocalDate yearEnd) {
    LocalDate enrollmentEnd =
        enrollment.getEndDate() != null ? enrollment.getEndDate() : LocalDate.MAX;
    return !enrollment.getStartDate().isAfter(yearEnd) && !enrollmentEnd.isBefore(yearStart);
  }
}
