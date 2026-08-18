package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.contract.CourseRequirementQuery;
import com.heigraduate.app.graduate.contract.FinalGradeQuery;
import com.heigraduate.app.graduate.dto.GraduationResult;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.EnrollmentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GraduationService {

  private static final int REQUIRED_YEARS = 3;

  private final EnrollmentRepository enrollmentRepository;
  private final AcademicYearRepository academicYearRepository;
  private final CourseRepository courseRepository;
  private final CourseRequirementQuery courseRequirementQuery;
  private final FinalGradeQuery finalGradeQuery;

  @Transactional(readOnly = true)
  public GraduationResult determineGraduation(UUID studentId) {
    List<Enrollment> history = enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId);
    if (history.isEmpty()) {
      throw new ResourceNotFoundException("No enrollment history for student: " + studentId);
    }

    // NC-05 FIX (§12) : limiter aux 3 premières années universitaires de l'étudiant.
    // Un étudiant ayant redoublé avec 4 années ne peut être diplômé que sur les 3 premières.
    List<AcademicYear> studentYears =
        academicYearRepository.findAll().stream()
            .filter(
                year ->
                    history.stream()
                        .anyMatch(e -> overlaps(e, year.getStartDate(), year.getEndDate())))
            .sorted(Comparator.comparing(AcademicYear::getStartDate))
            .limit(REQUIRED_YEARS)
            .toList();

    List<UUID> unvalidatedMandatoryCourses = new ArrayList<>();
    BigDecimal weightedSum = BigDecimal.ZERO;
    int gradedCredits = 0;
    int mandatoryCourseCount = 0;

    for (AcademicYear year : studentYears) {
      Set<UUID> parcoursIdsThisYear =
          history.stream()
              .filter(e -> overlaps(e, year.getStartDate(), year.getEndDate()))
              .map(Enrollment::getParcoursId)
              .collect(Collectors.toCollection(LinkedHashSet::new));

      // NC-02 FIX (§12) : dédupliquer les cours obligatoires par courseId.
      // Un cours commun à deux parcours (ex : commun → EL dans la même année) ne doit
      // être évalué qu'une seule fois. LinkedHashMap.put() écrase silencieusement les doublons.
      Map<UUID, Integer> mandatoryCoursesThisYear = new LinkedHashMap<>();
      for (UUID parcoursId : parcoursIdsThisYear) {
        List<UUID> mandatoryCourseIds =
            courseRequirementQuery.getMandatoryCourseIds(parcoursId, year.getId());
        for (UUID courseId : mandatoryCourseIds) {
          Course course =
              courseRepository
                  .findById(courseId)
                  .orElseThrow(
                      () -> new ResourceNotFoundException("Course not found: " + courseId));
          mandatoryCoursesThisYear.put(courseId, course.getCredits());
        }
      }

      for (Map.Entry<UUID, Integer> entry : mandatoryCoursesThisYear.entrySet()) {
        UUID courseId = entry.getKey();
        int credits = entry.getValue();
        mandatoryCourseCount++;

        Optional<BigDecimal> finalGrade = finalGradeQuery.getFinalGrade(studentId, courseId);

        if (finalGrade.isEmpty() || finalGrade.get().compareTo(BigDecimal.TEN) < 0) {
          unvalidatedMandatoryCourses.add(courseId);
          continue;
        }

        weightedSum = weightedSum.add(finalGrade.get().multiply(BigDecimal.valueOf(credits)));
        gradedCredits += credits;
      }
    }

    boolean completedThreeYears = studentYears.size() >= REQUIRED_YEARS;
    boolean graduated =
        completedThreeYears && mandatoryCourseCount > 0 && unvalidatedMandatoryCourses.isEmpty();
    BigDecimal overallAverage =
        gradedCredits == 0
            ? null
            : weightedSum.divide(BigDecimal.valueOf(gradedCredits), 2, RoundingMode.HALF_UP);

    return new GraduationResult(
        studentId,
        graduated,
        overallAverage,
        unvalidatedMandatoryCourses,
        studentYears.stream().map(AcademicYear::getId).toList());
  }

  private boolean overlaps(Enrollment enrollment, LocalDate yearStart, LocalDate yearEnd) {
    LocalDate enrollmentEnd =
        enrollment.getEndDate() != null ? enrollment.getEndDate() : LocalDate.MAX;
    return !enrollment.getStartDate().isAfter(yearEnd) && !enrollmentEnd.isBefore(yearStart);
  }
}
