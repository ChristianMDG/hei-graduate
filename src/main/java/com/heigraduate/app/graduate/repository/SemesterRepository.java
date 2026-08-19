package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Semester;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {

  @Query("SELECT s FROM Semester s WHERE s.startDate <= :endDate AND s.endDate >= :startDate")
  List<Semester> findOverlapping(LocalDate startDate, LocalDate endDate);

  @Query(
      "SELECT s FROM Semester s WHERE s.startDate <= :endDate AND s.endDate >= :startDate "
          + "AND s.id <> :excludeId")
  List<Semester> findOverlappingExcluding(LocalDate startDate, LocalDate endDate, UUID excludeId);
}
