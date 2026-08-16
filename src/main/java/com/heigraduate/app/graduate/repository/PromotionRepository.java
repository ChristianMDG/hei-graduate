package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Promotion;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    boolean existsByFinalAcademicYearId(UUID academicYearId);

    Optional<Promotion> findByFinalAcademicYearId(UUID academicYearId);
}