package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.DiplomaList;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiplomaListRepository extends JpaRepository<DiplomaList, UUID> {

  Optional<DiplomaList> findByPromotionIdAndParcoursId(UUID promotionId, UUID parcoursId);

  java.util.List<DiplomaList> findByPromotionId(UUID promotionId);
}
