package com.heigraduate.app.graduate.validator;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.repository.StudentRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentValidator {

    private final StudentRepository studentRepository;

    public void validateStudentNumberUniqueness(String studentNumber, UUID excludedId) {
        Optional<Student> existing = studentRepository.findByStudentNumberIgnoreCase(studentNumber.trim());
        if (existing.isPresent() && !existing.get().getId().equals(excludedId)) {
            throw new ConflictException("Student with number " + studentNumber + " already exists");
        }
    }

    public void validateUserNotAlreadyLinked(UUID userId, UUID excludedId) {
        Optional<Student> existing = studentRepository.findByUserId(userId);
        if (existing.isPresent() && !existing.get().getId().equals(excludedId)) {
            throw new ConflictException("This user is already linked to a student profile");
        }
    }
}