package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Semester;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {}
