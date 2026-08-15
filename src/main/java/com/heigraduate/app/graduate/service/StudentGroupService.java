package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.StudentGroupRequest;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.StudentGroup;
import com.heigraduate.app.graduate.repository.StudentGroupRepository;
import com.heigraduate.app.graduate.validator.StudentGroupValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentGroupService {

  private final StudentGroupRepository studentGroupRepository;
  private final StudentGroupValidator studentGroupValidator;

  @Transactional
  public StudentGroup create(StudentGroupRequest request) {
    studentGroupValidator.validateReferenceUniqueness(request.reference(), null);
    StudentGroup studentGroup =
        StudentGroup.builder()
            .reference(request.reference().trim().toUpperCase())
            .maxSize(request.maxSize())
            .build();
    return studentGroupRepository.save(studentGroup);
  }

  @Transactional
  public StudentGroup update(UUID id, StudentGroupRequest request) {
    StudentGroup existing = getOrThrow(id);
    studentGroupValidator.validateReferenceUniqueness(request.reference(), id);
    existing.setReference(request.reference().trim().toUpperCase());
    existing.setMaxSize(request.maxSize());
    return studentGroupRepository.save(existing);
  }

  @Transactional(readOnly = true)
  public StudentGroup getById(UUID id) {
    return getOrThrow(id);
  }

  @Transactional(readOnly = true)
  public List<StudentGroup> getAll() {
    return studentGroupRepository.findAll();
  }

  private StudentGroup getOrThrow(UUID id) {
    return studentGroupRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("StudentGroup not found with id: " + id));
  }
}
