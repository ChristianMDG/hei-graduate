package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.StudentRequest;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.repository.StudentRepository;
import com.heigraduate.app.graduate.validator.StudentValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentValidator studentValidator;

    @Transactional
    public Student create(StudentRequest request) {
        studentValidator.validateStudentNumberUniqueness(request.studentNumber(), null);
        studentValidator.validateUserNotAlreadyLinked(request.userId(), null);
        Student student =
                Student.builder()
                        .userId(request.userId())
                        .studentNumber(request.studentNumber().trim().toUpperCase())
                        .lastName(request.lastName().trim())
                        .firstName(request.firstName().trim())
                        .birthDate(request.birthDate())
                        .enrollmentDate(request.enrollmentDate())
                        .status(request.status())
                        .build();
        return studentRepository.save(student);
    }

    @Transactional
    public Student update(UUID id, StudentRequest request) {
        Student existing = getOrThrow(id);
        studentValidator.validateStudentNumberUniqueness(request.studentNumber(), id);
        studentValidator.validateUserNotAlreadyLinked(request.userId(), id);
        existing.setUserId(request.userId());
        existing.setStudentNumber(request.studentNumber().trim().toUpperCase());
        existing.setLastName(request.lastName().trim());
        existing.setFirstName(request.firstName().trim());
        existing.setBirthDate(request.birthDate());
        existing.setEnrollmentDate(request.enrollmentDate());
        existing.setStatus(request.status());
        return studentRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Student getById(UUID id) {
        return getOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    private Student getOrThrow(UUID id) {
        return studentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }
}