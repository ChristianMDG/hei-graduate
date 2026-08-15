package com.heigraduate.app.graduate.validator;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.model.Teacher;
import com.heigraduate.app.graduate.repository.TeacherRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeacherValidator {

  private final TeacherRepository teacherRepository;

  public void validateUserNotAlreadyLinked(UUID userId, UUID excludedId) {
    Optional<Teacher> existing = teacherRepository.findByUserId(userId);
    if (existing.isPresent() && !existing.get().getId().equals(excludedId)) {
      throw new ConflictException("This user is already linked to a teacher profile");
    }
  }
}
