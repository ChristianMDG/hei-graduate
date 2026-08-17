package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Diploma;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DiplomaRepository extends JpaRepository<Diploma, UUID> {

  List<Diploma> findByPromotionIdAndParcoursIdOrderByRankAsc(UUID promotionId, UUID parcoursId);

  void deleteByPromotionIdAndParcoursId(UUID promotionId, UUID parcoursId);

  @Query("SELECT DISTINCT d.parcoursId FROM Diploma d WHERE d.promotionId = :promotionId")
  List<UUID> findDistinctParcoursIdsByPromotionId(UUID promotionId);
}
