package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.StudentGroup;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentGroupRepository extends JpaRepository<StudentGroup, UUID> {

  Optional<StudentGroup> findByReferenceIgnoreCase(String reference);

  boolean existsByReferenceIgnoreCase(String reference);
}
