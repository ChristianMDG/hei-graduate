package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.TeacherRequest;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.Teacher;
import com.heigraduate.app.graduate.repository.TeacherRepository;
import com.heigraduate.app.graduate.validator.TeacherValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeacherService {

  private final TeacherRepository teacherRepository;
  private final TeacherValidator teacherValidator;

  @Transactional
  public Teacher create(TeacherRequest request) {
    teacherValidator.validateUserNotAlreadyLinked(request.userId(), null);
    Teacher teacher =
        Teacher.builder()
            .userId(request.userId())
            .lastName(request.lastName().trim())
            .firstName(request.firstName().trim())
            .specialty(request.specialty())
            .contractType(request.contractType())
            .build();
    return teacherRepository.save(teacher);
  }

  @Transactional
  public Teacher update(UUID id, TeacherRequest request) {
    Teacher existing = getOrThrow(id);
    teacherValidator.validateUserNotAlreadyLinked(request.userId(), id);
    existing.setUserId(request.userId());
    existing.setLastName(request.lastName().trim());
    existing.setFirstName(request.firstName().trim());
    existing.setSpecialty(request.specialty());
    existing.setContractType(request.contractType());
    return teacherRepository.save(existing);
  }

  @Transactional(readOnly = true)
  public Teacher getById(UUID id) {
    return getOrThrow(id);
  }

  @Transactional(readOnly = true)
  public List<Teacher> getAll() {
    return teacherRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Teacher getByCurrentUser(UUID connectedUserId) {
    return teacherRepository
        .findByUserId(connectedUserId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "No teacher profile linked to user: " + connectedUserId));
  }

  private Teacher getOrThrow(UUID id) {
    return teacherRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
  }
}
