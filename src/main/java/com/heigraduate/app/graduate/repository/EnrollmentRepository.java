package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Enrollment;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

  List<Enrollment> findByStudentIdOrderByStartDateAsc(UUID studentId);

  Optional<Enrollment> findByStudentIdAndEndDateIsNull(UUID studentId);

  List<Enrollment> findByParcoursId(UUID parcoursId);

  @org.springframework.data.jpa.repository.Query(
      "SELECT e FROM Enrollment e WHERE e.studentId = :studentId "
          + "AND e.startDate <= :date "
          + "AND (e.endDate IS NULL OR e.endDate >= :date)")
  Optional<Enrollment> findActiveAt(UUID studentId, LocalDate date);
}
