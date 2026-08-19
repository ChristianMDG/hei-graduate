package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.dto.StudentRequest;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.repository.StudentRepository;
import com.heigraduate.app.graduate.service.StudentService;
import com.heigraduate.app.graduate.validator.StudentValidator;
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
class StudentServiceTest {

  @Mock private StudentRepository studentRepository;
  @Mock private StudentValidator studentValidator;

  @InjectMocks private StudentService studentService;

  private Student student;
  private UUID studentId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    studentId = UUID.randomUUID();
    userId = UUID.randomUUID();
    student =
        Student.builder()
            .id(studentId)
            .userId(userId)
            .studentNumber("STD001")
            .lastName("Dupont")
            .firstName("Jean")
            .birthDate(LocalDate.of(2005, 3, 15))
            .enrollmentDate(LocalDate.of(2023, 9, 1))
            .status("ACTIVE")
            .build();
  }

  @Test
  void create_shouldPersistStudent_whenDataIsValid() {
    StudentRequest request =
        new StudentRequest(
            userId,
            "STD001",
            "Dupont",
            "Jean",
            LocalDate.of(2005, 3, 15),
            LocalDate.of(2023, 9, 1),
            "ACTIVE");

    when(studentRepository.save(any(Student.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Student result = studentService.create(request);

    assertThat(result.getStudentNumber()).isEqualTo("STD001");
    assertThat(result.getLastName()).isEqualTo("Dupont");
    assertThat(result.getFirstName()).isEqualTo("Jean");
    verify(studentRepository).save(any(Student.class));
    verify(studentValidator).validateStudentNumberUniqueness("STD001", null);
    verify(studentValidator).validateUserNotAlreadyLinked(userId, null);
  }

  @Test
  void create_shouldThrowConflict_whenStudentNumberAlreadyExists() {
    StudentRequest request =
        new StudentRequest(
            userId,
            "STD001",
            "Dupont",
            "Jean",
            LocalDate.of(2005, 3, 15),
            LocalDate.of(2023, 9, 1),
            "ACTIVE");

    doThrow(new ConflictException("Student number STD001 already exists"))
        .when(studentValidator)
        .validateStudentNumberUniqueness("STD001", null);

    assertThatThrownBy(() -> studentService.create(request))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("STD001");

    verify(studentRepository, never()).save(any());
  }

  @Test
  void create_shouldThrowConflict_whenUserIdAlreadyLinked() {
    StudentRequest request =
        new StudentRequest(
            userId,
            "STD001",
            "Dupont",
            "Jean",
            LocalDate.of(2005, 3, 15),
            LocalDate.of(2023, 9, 1),
            "ACTIVE");

    doThrow(new ConflictException("User already linked to a student"))
        .when(studentValidator)
        .validateUserNotAlreadyLinked(userId, null);

    assertThatThrownBy(() -> studentService.create(request)).isInstanceOf(ConflictException.class);

    verify(studentRepository, never()).save(any());
  }

  @Test
  void update_shouldModifyExistingStudent() {
    StudentRequest request =
        new StudentRequest(
            userId,
            "STD001",
            "Durand",
            "Pierre",
            LocalDate.of(2005, 3, 15),
            LocalDate.of(2023, 9, 1),
            "ACTIVE");

    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(studentRepository.save(any(Student.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Student result = studentService.update(studentId, request);

    assertThat(result.getLastName()).isEqualTo("Durand");
    assertThat(result.getFirstName()).isEqualTo("Pierre");
    verify(studentRepository).save(any(Student.class));
  }

  @Test
  void update_shouldThrow_whenStudentNotFound() {
    UUID unknownId = UUID.randomUUID();
    StudentRequest request =
        new StudentRequest(
            userId,
            "STD001",
            "Dupont",
            "Jean",
            LocalDate.of(2005, 3, 15),
            LocalDate.of(2023, 9, 1),
            "ACTIVE");

    when(studentRepository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> studentService.update(unknownId, request))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(studentRepository, never()).save(any());
  }

  @Test
  void getById_shouldReturnStudent_whenExists() {
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

    Student result = studentService.getById(studentId);

    assertThat(result.getId()).isEqualTo(studentId);
    assertThat(result.getStudentNumber()).isEqualTo("STD001");
  }

  @Test
  void getById_shouldThrow_whenNotFound() {
    UUID unknownId = UUID.randomUUID();
    when(studentRepository.findById(unknownId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> studentService.getById(unknownId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getAll_shouldReturnAllStudents() {
    when(studentRepository.findAll()).thenReturn(List.of(student));

    List<Student> result = studentService.getAll();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getStudentNumber()).isEqualTo("STD001");
  }
}
