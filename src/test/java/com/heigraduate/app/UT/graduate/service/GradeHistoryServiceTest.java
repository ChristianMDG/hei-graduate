package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.heigraduate.app.graduate.dto.GradeResponse;
import com.heigraduate.app.graduate.dto.GradeUpdateRequest;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.Exam;
import com.heigraduate.app.graduate.model.Grade;
import com.heigraduate.app.graduate.model.GradeHistory;
import com.heigraduate.app.graduate.model.GradeStatus;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.repository.ExamRepository;
import com.heigraduate.app.graduate.repository.GradeHistoryRepository;
import com.heigraduate.app.graduate.repository.GradeRepository;
import com.heigraduate.app.graduate.repository.StudentRepository;
import com.heigraduate.app.graduate.service.GradeService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GradeHistoryServiceTest {

  @Mock private GradeRepository gradeRepository;
  @Mock private GradeHistoryRepository gradeHistoryRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private ExamRepository examRepository;

  @InjectMocks private GradeService gradeService;

  private Grade grade;
  private UUID gradeId;
  private UUID actingUserId;

  @BeforeEach
  void setUp() {
    gradeId = UUID.randomUUID();
    actingUserId = UUID.randomUUID();

    Student student =
        Student.builder()
            .id(UUID.randomUUID())
            .userId(UUID.randomUUID())
            .studentNumber("STD24049")
            .lastName("Lahatra")
            .firstName("Nomena")
            .enrollmentDate(LocalDate.of(2023, 9, 1))
            .status("ACTIVE")
            .build();

    Exam exam =
        Exam.builder()
            .id(UUID.randomUUID())
            .course(
                Course.builder()
                    .id(UUID.randomUUID())
                    .courseReference("PROG4")
                    .title("Programmation 4")
                    .credits(5)
                    .active(true)
                    .build())
            .label("Controle continu")
            .coefficient(new BigDecimal("0.4"))
            .build();

    grade =
        Grade.builder()
            .id(gradeId)
            .student(student)
            .exam(exam)
            .value(new BigDecimal("8.50"))
            .status(GradeStatus.PUBLISHED)
            .build();
  }

  @Test
  void update_shouldReproduceExactSubjectExample_readOldValue_thenInsertHistory_thenUpdate() {
    GradeUpdateRequest request =
        new GradeUpdateRequest(
            new BigDecimal("10.50"), "Erreur de saisie corrigee apres reclamation");

    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
    when(gradeRepository.save(any(Grade.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(gradeHistoryRepository.save(any(GradeHistory.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    GradeResponse result = gradeService.update(gradeId, request, actingUserId);

    assertThat(result.value()).isEqualByComparingTo("10.50");

    ArgumentCaptor<GradeHistory> historyCaptor = ArgumentCaptor.forClass(GradeHistory.class);
    verify(gradeHistoryRepository).save(historyCaptor.capture());

    GradeHistory savedHistory = historyCaptor.getValue();
    assertThat(savedHistory.getOldValue()).isEqualByComparingTo("8.50");
    assertThat(savedHistory.getNewValue()).isEqualByComparingTo("10.50");
    assertThat(savedHistory.getReason()).isEqualTo("Erreur de saisie corrigee apres reclamation");
    assertThat(savedHistory.getChangedByUserId()).isEqualTo(actingUserId);
    assertThat(savedHistory.getChangedAt()).isNotNull();

    var inOrder = inOrder(gradeHistoryRepository, gradeRepository);
    inOrder.verify(gradeHistoryRepository).save(any(GradeHistory.class));
    inOrder.verify(gradeRepository).save(any(Grade.class));
  }

  @Test
  void update_shouldThrow_whenGradeNotFound() {
    UUID unknownId = UUID.randomUUID();
    GradeUpdateRequest request = new GradeUpdateRequest(new BigDecimal("12.00"), "Correction");

    when(gradeRepository.findById(unknownId)).thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class,
        () -> gradeService.update(unknownId, request, actingUserId));

    verify(gradeHistoryRepository, never()).save(any());
    verify(gradeRepository, never()).save(any());
  }

  @Test
  void getHistory_shouldReturnAllChangesForGrade() {
    GradeHistory pastChange =
        GradeHistory.builder()
            .id(UUID.randomUUID())
            .grade(grade)
            .oldValue(new BigDecimal("7.00"))
            .newValue(new BigDecimal("8.50"))
            .reason("Premiere correction")
            .changedByUserId(actingUserId)
            .build();

    when(gradeRepository.existsById(gradeId)).thenReturn(true);
    when(gradeHistoryRepository.findByGradeId(gradeId)).thenReturn(List.of(pastChange));

    var result = gradeService.getHistory(gradeId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).oldValue()).isEqualByComparingTo("7.00");
    assertThat(result.get(0).newValue()).isEqualByComparingTo("8.50");
  }
}
