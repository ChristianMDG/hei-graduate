package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.DiplomaResponse;
import com.heigraduate.app.graduate.dto.GraduationResult;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.mapper.DiplomaMapper;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Diploma;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.model.Promotion;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.repository.DiplomaRepository;
import com.heigraduate.app.graduate.repository.EnrollmentRepository;
import com.heigraduate.app.graduate.repository.PromotionRepository;
import com.heigraduate.app.graduate.repository.StudentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingService {

  private final PromotionRepository promotionRepository;
  private final DiplomaRepository diplomaRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final StudentRepository studentRepository;
  private final GraduationService graduationService;

  @Transactional
  public List<DiplomaResponse> generateRanking(UUID promotionId, UUID parcoursId) {
    Promotion promotion =
        promotionRepository
            .findById(promotionId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Promotion not found: " + promotionId));

    AcademicYear finalYear = promotion.getFinalAcademicYear();

    List<UUID> candidateStudentIds =
        enrollmentRepository.findByParcoursId(parcoursId).stream()
            .filter(e -> overlaps(e, finalYear.getStartDate(), finalYear.getEndDate()))
            .map(Enrollment::getStudentId)
            .distinct()
            .toList();

    List<GraduatedCandidate> graduatedCandidates = new ArrayList<>();
    for (UUID studentId : candidateStudentIds) {
      GraduationResult result = graduationService.determineGraduation(studentId);
      if (result.graduated()) {
        graduatedCandidates.add(new GraduatedCandidate(studentId, result.overallAverage()));
      }
    }

    graduatedCandidates.sort(Comparator.comparing(GraduatedCandidate::average).reversed());

    diplomaRepository.deleteByPromotionIdAndParcoursId(promotionId, parcoursId);

    List<Diploma> diplomas = new ArrayList<>();
    int rank = 1;
    for (GraduatedCandidate candidate : graduatedCandidates) {
      Diploma diploma =
          Diploma.builder()
              .studentId(candidate.studentId())
              .promotionId(promotionId)
              .parcoursId(parcoursId)
              .obtainedDate(LocalDate.now())
              .overallAverage(candidate.average())
              .rank(rank)
              .mention(computeMention(candidate.average()))
              .build();
      diplomas.add(diplomaRepository.save(diploma));
      rank++;
    }
    return toResponses(diplomas);
  }

  @Transactional(readOnly = true)
  public List<DiplomaResponse> getRanking(UUID promotionId, UUID parcoursId) {
    List<Diploma> diplomas =
        diplomaRepository.findByPromotionIdAndParcoursIdOrderByRankAsc(promotionId, parcoursId);
    return toResponses(diplomas);
  }

  private List<DiplomaResponse> toResponses(List<Diploma> diplomas) {
    if (diplomas.isEmpty()) {
      return List.of();
    }
    List<UUID> studentIds = diplomas.stream().map(Diploma::getStudentId).distinct().toList();
    Map<UUID, Student> studentsById =
        studentRepository.findAllById(studentIds).stream()
            .collect(Collectors.toMap(Student::getId, Function.identity()));

    return diplomas.stream()
        .map(
            diploma -> {
              Student student = studentsById.get(diploma.getStudentId());
              if (student == null) {
                throw new ResourceNotFoundException(
                    "Student not found with id: " + diploma.getStudentId());
              }
              return DiplomaMapper.toResponse(diploma, student);
            })
        .toList();
  }

  private String computeMention(BigDecimal average) {
    if (average.compareTo(BigDecimal.valueOf(16)) >= 0) return "Tres Bien";
    if (average.compareTo(BigDecimal.valueOf(14)) >= 0) return "Bien";
    if (average.compareTo(BigDecimal.valueOf(12)) >= 0) return "Assez Bien";
    return "Passable";
  }

  private boolean overlaps(Enrollment enrollment, LocalDate yearStart, LocalDate yearEnd) {
    LocalDate effectiveEnd =
        enrollment.getEndDate() != null ? enrollment.getEndDate() : LocalDate.MAX;
    return !enrollment.getStartDate().isAfter(yearEnd) && !effectiveEnd.isBefore(yearStart);
  }

  private record GraduatedCandidate(UUID studentId, BigDecimal average) {}
}
