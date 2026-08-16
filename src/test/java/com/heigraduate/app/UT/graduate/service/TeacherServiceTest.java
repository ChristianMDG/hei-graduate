package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.dto.TeacherRequest;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.Teacher;
import com.heigraduate.app.graduate.repository.TeacherRepository;
import com.heigraduate.app.graduate.service.TeacherService;
import com.heigraduate.app.graduate.validator.TeacherValidator;
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
class TeacherServiceTest {

  @Mock private TeacherRepository teacherRepository;
  @Mock private TeacherValidator teacherValidator;

  @InjectMocks private TeacherService teacherService;

  private Teacher teacher;
  private UUID teacherId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    teacherId = UUID.randomUUID();
    userId = UUID.randomUUID();
    teacher =
        Teacher.builder()
            .id(teacherId)
            .userId(userId)
            .lastName("Martin")
            .firstName("Sophie")
            .specialty("Algorithmique")
            .contractType("CDI")
            .build();
  }

  @Test
  void create_shouldPersistTeacher_whenDataIsValid() {
    TeacherRequest request = new TeacherRequest(userId, "Martin", "Sophie", "Algorithmique", "CDI");

    when(teacherRepository.save(any(Teacher.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Teacher result = teacherService.create(request);

    assertThat(result.getLastName()).isEqualTo("Martin");
    assertThat(result.getFirstName()).isEqualTo("Sophie");
    assertThat(result.getSpecialty()).isEqualTo("Algorithmique");
    verify(teacherRepository).save(any(Teacher.class));
    verify(teacherValidator).validateUserNotAlreadyLinked(userId, null);
  }

  @Test
  void create_shouldThrowConflict_whenUserIdAlreadyLinked() {
    TeacherRequest request = new TeacherRequest(userId, "Martin", "Sophie", "Algorithmique", "CDI");

    // FIX: Utiliser doThrow() pour les méthodes void
    doThrow(new ConflictException("User already linked to a teacher"))
        .when(teacherValidator)
        .validateUserNotAlreadyLinked(userId, null);

    assertThatThrownBy(() -> teacherService.create(request)).isInstanceOf(ConflictException.class);

    verify(teacherRepository, never()).save(any());
  }

  @Test
  void update_shouldModifyExistingTeacher() {
    TeacherRequest request =
        new TeacherRequest(userId, "Bernard", "Paul", "Bases de données", "CDD");

    when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
    when(teacherRepository.save(any(Teacher.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Teacher result = teacherService.update(teacherId, request);

    assertThat(result.getLastName()).isEqualTo("Bernard");
    assertThat(result.getFirstName()).isEqualTo("Paul");
    assertThat(result.getSpecialty()).isEqualTo("Bases de données");
    verify(teacherRepository).save(any(Teacher.class));
  }

  @Test
  void update_shouldThrow_whenTeacherNotFound() {
    UUID unknownId = UUID.randomUUID();
    TeacherRequest request = new TeacherRequest(userId, "Martin", "Sophie", "Algorithmique", "CDI");

    when(teacherRepository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> teacherService.update(unknownId, request))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(teacherRepository, never()).save(any());
  }

  @Test
  void getById_shouldReturnTeacher_whenExists() {
    when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

    Teacher result = teacherService.getById(teacherId);

    assertThat(result.getId()).isEqualTo(teacherId);
    assertThat(result.getLastName()).isEqualTo("Martin");
  }

  @Test
  void getById_shouldThrow_whenNotFound() {
    UUID unknownId = UUID.randomUUID();
    when(teacherRepository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> teacherService.getById(unknownId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getAll_shouldReturnAllTeachers() {
    when(teacherRepository.findAll()).thenReturn(List.of(teacher));

    List<Teacher> result = teacherService.getAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getLastName()).isEqualTo("Martin");
  }
}
