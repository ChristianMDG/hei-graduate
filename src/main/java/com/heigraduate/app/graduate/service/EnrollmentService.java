package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.EnrollmentRequest;
import com.heigraduate.app.graduate.dto.TransferRequest;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.repository.EnrollmentRepository;
import com.heigraduate.app.graduate.validator.EnrollmentValidator;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

  private final EnrollmentRepository enrollmentRepository;
  private final EnrollmentValidator enrollmentValidator;

  @Transactional
  public Enrollment enroll(EnrollmentRequest request) {
    enrollmentValidator.validateNoActiveEnrollment(request.studentId());
    Enrollment enrollment =
        Enrollment.builder()
            .studentId(request.studentId())
            .parcoursId(request.parcoursId())
            .studentGroupId(request.studentGroupId())
            .startDate(request.startDate())
            .endDate(null)
            .build();
    return enrollmentRepository.save(enrollment);
  }

  @Transactional
  public Enrollment transfer(UUID studentId, TransferRequest request) {
    Enrollment currentActive =
        enrollmentRepository
            .findByStudentIdAndEndDateIsNull(studentId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No active enrollment found for student: " + studentId));

    enrollmentValidator.validateTransferDate(currentActive, request.effectiveDate());

    currentActive.setEndDate(request.effectiveDate());
    enrollmentRepository.save(currentActive);

    Enrollment newEnrollment =
        Enrollment.builder()
            .studentId(studentId)
            .parcoursId(
                request.parcoursId() != null ? request.parcoursId() : currentActive.getParcoursId())
            .studentGroupId(
                request.studentGroupId() != null
                    ? request.studentGroupId()
                    : currentActive.getStudentGroupId())
            .startDate(request.effectiveDate())
            .endDate(null)
            .build();
    return enrollmentRepository.save(newEnrollment);
  }

  @Transactional(readOnly = true)
  public Enrollment getCurrent(UUID studentId) {
    return enrollmentRepository
        .findByStudentIdAndEndDateIsNull(studentId)
        .orElseThrow(
            () -> new ResourceNotFoundException("No active enrollment for student: " + studentId));
  }

  @Transactional(readOnly = true)
  public List<Enrollment> getHistory(UUID studentId) {
    return enrollmentRepository.findByStudentIdOrderByStartDateAsc(studentId);
  }

  @Transactional(readOnly = true)
  public Enrollment getActiveAt(UUID studentId, LocalDate date) {
    return enrollmentRepository
        .findActiveAt(studentId, date)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "No enrollment found for student " + studentId + " on date " + date));
  }
}
