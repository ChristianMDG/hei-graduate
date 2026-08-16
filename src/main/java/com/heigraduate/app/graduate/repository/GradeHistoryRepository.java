package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.GradeHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeHistoryRepository extends JpaRepository<GradeHistory, UUID> {

  List<GradeHistory> findByGradeId(UUID gradeId);
}
