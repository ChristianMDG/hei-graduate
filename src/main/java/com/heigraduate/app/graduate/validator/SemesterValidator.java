package com.heigraduate.app.graduate.validator;

import com.heigraduate.app.graduate.exception.BadRequestException;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Semester;
import com.heigraduate.app.graduate.repository.SemesterRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SemesterValidator {

  private final SemesterRepository semesterRepository;

  public void validateDateRange(LocalDate startDate, LocalDate endDate) {
    if (!startDate.isBefore(endDate)) {
      throw new BadRequestException("startDate must be before endDate");
    }
  }

  /**
   * MCD v2, correction #4: a semester belongs to exactly one academic year, so its date range must
   * fall within that academic year's date range - otherwise the FK is technically present but
   * semantically meaningless.
   */
  public void validateWithinAcademicYear(
      AcademicYear academicYear, LocalDate startDate, LocalDate endDate) {
    if (startDate.isBefore(academicYear.getStartDate())
        || endDate.isAfter(academicYear.getEndDate())) {
      throw new BadRequestException(
          "Semester date range must be within its academic year (" + academicYear.getLabel() + ")");
    }
  }

  public void validateNoOverlap(LocalDate startDate, LocalDate endDate, UUID excludedId) {
    List<Semester> overlapping = semesterRepository.findOverlapping(startDate, endDate, excludedId);
    if (!overlapping.isEmpty()) {
      throw new ConflictException(
          "This date range overlaps with an existing semester: " + overlapping.get(0).getLabel());
    }
  }
}
