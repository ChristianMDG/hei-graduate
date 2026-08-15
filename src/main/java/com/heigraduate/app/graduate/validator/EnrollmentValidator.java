package com.heigraduate.app.graduate.validator;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.repository.EnrollmentRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentValidator {

    private final EnrollmentRepository enrollmentRepository;

    public void validateNoActiveEnrollment(UUID studentId) {
        Optional<Enrollment> active = enrollmentRepository.findByStudentIdAndEndDateIsNull(studentId);
        if (active.isPresent()) {
            throw new ConflictException(
                    "Student already has an active enrollment starting "
                            + active.get().getStartDate()
                            + " — use the transfer endpoint instead of creating a new enrollment");
        }
    }

    public void validateTransferDate(Enrollment currentActive, LocalDate effectiveDate) {
        if (!effectiveDate.isAfter(currentActive.getStartDate())) {
            throw new ConflictException(
                    "effectiveDate must be after the current enrollment's start date ("
                            + currentActive.getStartDate()
                            + ")");
        }
    }
}