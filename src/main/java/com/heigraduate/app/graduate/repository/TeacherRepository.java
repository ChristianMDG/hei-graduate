package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Teacher;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

  Optional<Teacher> findByUserId(UUID userId);

  boolean existsByUserId(UUID userId);
}
