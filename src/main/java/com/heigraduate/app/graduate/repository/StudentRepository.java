package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Student;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Optional<Student> findByStudentNumberIgnoreCase(String studentNumber);

    boolean existsByStudentNumberIgnoreCase(String studentNumber);

    Optional<Student> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}