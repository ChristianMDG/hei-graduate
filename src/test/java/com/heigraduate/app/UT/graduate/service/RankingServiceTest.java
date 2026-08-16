package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.heigraduate.app.graduate.dto.GraduationResult;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Diploma;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.model.Promotion;
import com.heigraduate.app.graduate.repository.DiplomaRepository;
import com.heigraduate.app.graduate.repository.EnrollmentRepository;
import com.heigraduate.app.graduate.repository.PromotionRepository;
import com.heigraduate.app.graduate.service.GraduationService;
import com.heigraduate.app.graduate.service.RankingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

  @Mock private PromotionRepository promotionRepository;
  @Mock private DiplomaRepository diplomaRepository;
  @Mock private EnrollmentRepository enrollmentRepository;
  @Mock private GraduationService graduationService;

  @InjectMocks private RankingService rankingService;

  private UUID promotionId;
  private UUID parcoursId;
  private AcademicYear finalYear;
  private Promotion promotion;

  @BeforeEach
  void setUp() {
    promotionId = UUID.randomUUID();
    parcoursId = UUID.randomUUID();
    finalYear =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 6, 30))
            .level("L3")
            .build();
    promotion =
        Promotion.builder()
            .id(promotionId)
            .label("Promo 2026")
            .finalAcademicYear(finalYear)
            .build();
  }

  private Enrollment enrollment(UUID studentId, UUID parcours) {
    return Enrollment.builder()
        .id(UUID.randomUUID())
        .studentId(studentId)
        .parcoursId(parcours)
        .groupId(UUID.randomUUID())
        .startDate(LocalDate.of(2025, 9, 1))
        .endDate(null)
        .build();
  }

  @Test
  void generateRanking_shouldOnlyKeepGraduatedStudents_rankedByDescendingAverage() {
    UUID studentJean = UUID.randomUUID();
    UUID studentMarie = UUID.randomUUID();
    UUID studentPaul = UUID.randomUUID();
    UUID studentNonGraduated = UUID.randomUUID();
    UUID otherParcoursStudent = UUID.randomUUID();

    when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));
    when(enrollmentRepository.findAll())
        .thenReturn(
            List.of(
                enrollment(studentJean, parcoursId),
                enrollment(studentMarie, parcoursId),
                enrollment(studentPaul, parcoursId),
                enrollment(studentNonGraduated, parcoursId),
                enrollment(otherParcoursStudent, UUID.randomUUID())));

    when(graduationService.determineGraduation(studentJean))
        .thenReturn(
            new GraduationResult(studentJean, true, new BigDecimal("16.42"), List.of(), List.of()));
    when(graduationService.determineGraduation(studentMarie))
        .thenReturn(
            new GraduationResult(
                studentMarie, true, new BigDecimal("15.87"), List.of(), List.of()));
    when(graduationService.determineGraduation(studentPaul))
        .thenReturn(
            new GraduationResult(studentPaul, true, new BigDecimal("15.21"), List.of(), List.of()));
    when(graduationService.determineGraduation(studentNonGraduated))
        .thenReturn(
            new GraduationResult(
                studentNonGraduated,
                false,
                new BigDecimal("9.00"),
                List.of(UUID.randomUUID()),
                List.of()));

    when(diplomaRepository.save(any(Diploma.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    List<Diploma> ranking = rankingService.generateRanking(promotionId, parcoursId);

    assertThat(ranking).hasSize(3);
    assertThat(ranking.get(0).getStudentId()).isEqualTo(studentJean);
    assertThat(ranking.get(0).getRank()).isEqualTo(1);
    assertThat(ranking.get(1).getStudentId()).isEqualTo(studentMarie);
    assertThat(ranking.get(1).getRank()).isEqualTo(2);
    assertThat(ranking.get(2).getStudentId()).isEqualTo(studentPaul);
    assertThat(ranking.get(2).getRank()).isEqualTo(3);

    verify(graduationService, never()).determineGraduation(otherParcoursStudent);
    verify(diplomaRepository).deleteByPromotionIdAndParcoursId(promotionId, parcoursId);
  }

  @Test
  void generateRanking_shouldComputeMentionFromAverage() {
    UUID excellent = UUID.randomUUID();
    UUID bien = UUID.randomUUID();
    UUID assezBien = UUID.randomUUID();
    UUID passable = UUID.randomUUID();

    when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));
    when(enrollmentRepository.findAll())
        .thenReturn(
            List.of(
                enrollment(excellent, parcoursId),
                enrollment(bien, parcoursId),
                enrollment(assezBien, parcoursId),
                enrollment(passable, parcoursId)));

    when(graduationService.determineGraduation(excellent))
        .thenReturn(
            new GraduationResult(excellent, true, new BigDecimal("17.00"), List.of(), List.of()));
    when(graduationService.determineGraduation(bien))
        .thenReturn(
            new GraduationResult(bien, true, new BigDecimal("14.50"), List.of(), List.of()));
    when(graduationService.determineGraduation(assezBien))
        .thenReturn(
            new GraduationResult(assezBien, true, new BigDecimal("12.10"), List.of(), List.of()));
    when(graduationService.determineGraduation(passable))
        .thenReturn(
            new GraduationResult(passable, true, new BigDecimal("10.50"), List.of(), List.of()));
    when(diplomaRepository.save(any(Diploma.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    List<Diploma> ranking = rankingService.generateRanking(promotionId, parcoursId);

    assertThat(ranking)
        .extracting(Diploma::getStudentId, Diploma::getMention)
        .containsExactly(
            tuple(excellent, "Tres Bien"),
            tuple(bien, "Bien"),
            tuple(assezBien, "Assez Bien"),
            tuple(passable, "Passable"));
  }

  @Test
  void generateRanking_shouldReturnEmptyList_whenNoStudentGraduated() {
    UUID studentId = UUID.randomUUID();

    when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));
    when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment(studentId, parcoursId)));
    when(graduationService.determineGraduation(studentId))
        .thenReturn(
            new GraduationResult(
                studentId, false, new BigDecimal("8.00"), List.of(UUID.randomUUID()), List.of()));

    List<Diploma> ranking = rankingService.generateRanking(promotionId, parcoursId);

    assertThat(ranking).isEmpty();
    verify(diplomaRepository).deleteByPromotionIdAndParcoursId(promotionId, parcoursId);
    verify(diplomaRepository, never()).save(any());
  }

  @Test
  void generateRanking_shouldThrow_whenPromotionNotFound() {
    when(promotionRepository.findById(promotionId)).thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class,
        () -> rankingService.generateRanking(promotionId, parcoursId));
  }

  @Test
  void getRanking_shouldDelegateToRepositoryOrderedByRank() {
    Diploma diploma =
        Diploma.builder()
            .id(UUID.randomUUID())
            .studentId(UUID.randomUUID())
            .promotionId(promotionId)
            .parcoursId(parcoursId)
            .obtainedDate(LocalDate.now())
            .overallAverage(new BigDecimal("16.00"))
            .rank(1)
            .mention("Tres Bien")
            .build();
    when(diplomaRepository.findByPromotionIdAndParcoursIdOrderByRankAsc(promotionId, parcoursId))
        .thenReturn(List.of(diploma));

    List<Diploma> result = rankingService.getRanking(promotionId, parcoursId);

    assertThat(result).containsExactly(diploma);
  }
}
