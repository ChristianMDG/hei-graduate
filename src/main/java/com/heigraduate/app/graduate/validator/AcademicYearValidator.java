package com.heigraduate.app.graduate.validator;

import com.heigraduate.app.graduate.exception.ConflictException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AcademicYearValidator {

    public void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (!startDate.isBefore(endDate)) {
            throw new ConflictException("startDate must be before endDate");
        }
    }
}