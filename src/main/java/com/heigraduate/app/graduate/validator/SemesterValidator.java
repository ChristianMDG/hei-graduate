package com.heigraduate.app.graduate.validator;

import com.heigraduate.app.graduate.exception.BadRequestException;
import com.heigraduate.app.graduate.exception.ConflictException;
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

  public void validateNoOverlap(LocalDate startDate, LocalDate endDate, UUID excludedId) {
    List<Semester> overlapping = semesterRepository.findOverlapping(startDate, endDate, excludedId);
    if (!overlapping.isEmpty()) {
      throw new ConflictException(
          "This date range overlaps with an existing semester: " + overlapping.get(0).getLabel());
    }
  }
}
