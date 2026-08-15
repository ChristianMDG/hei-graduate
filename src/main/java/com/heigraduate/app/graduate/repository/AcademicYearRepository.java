package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.AcademicYear;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {

    List<AcademicYear> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate date, LocalDate sameDate);
}