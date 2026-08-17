package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.SemesterRequest;
import com.heigraduate.app.graduate.dto.SemesterResponse;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.mapper.SemesterMapper;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Semester;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.SemesterRepository;
import com.heigraduate.app.graduate.validator.SemesterValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SemesterService {

  private final SemesterRepository semesterRepository;
  private final AcademicYearRepository academicYearRepository;
  private final SemesterValidator semesterValidator;

  @Transactional(readOnly = true)
  public List<SemesterResponse> findAll() {
    return semesterRepository.findAll().stream().map(SemesterMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public SemesterResponse findById(UUID id) {
    return semesterRepository
        .findById(id)
        .map(SemesterMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + id));
  }

  @Transactional
  public SemesterResponse create(SemesterRequest request) {
    semesterValidator.validateDateRange(request.startDate(), request.endDate());
    semesterValidator.validateNoOverlap(request.startDate(), request.endDate(), null);
    AcademicYear academicYear = getAcademicYearOrThrow(request.academicYearId());
    semesterValidator.validateWithinAcademicYear(
        academicYear, request.startDate(), request.endDate());

    Semester semester =
        Semester.builder()
            .academicYear(academicYear)
            .label(request.label())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .expectedCredits(request.expectedCredits())
            .active(true)
            .build();
    return SemesterMapper.toResponse(semesterRepository.save(semester));
  }

  @Transactional
  public SemesterResponse update(UUID id, SemesterRequest request) {
    semesterValidator.validateDateRange(request.startDate(), request.endDate());
    semesterValidator.validateNoOverlap(request.startDate(), request.endDate(), id);
    AcademicYear academicYear = getAcademicYearOrThrow(request.academicYearId());
    semesterValidator.validateWithinAcademicYear(
        academicYear, request.startDate(), request.endDate());

    Semester semester =
        semesterRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + id));
    semester.setAcademicYear(academicYear);
    semester.setLabel(request.label());
    semester.setStartDate(request.startDate());
    semester.setEndDate(request.endDate());
    semester.setExpectedCredits(request.expectedCredits());
    return SemesterMapper.toResponse(semesterRepository.save(semester));
  }

  private AcademicYear getAcademicYearOrThrow(UUID academicYearId) {
    return academicYearRepository
        .findById(academicYearId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("AcademicYear not found with id: " + academicYearId));
  }

  @Transactional
  public void deactivate(UUID id) {
    Semester semester =
        semesterRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + id));
    semester.setActive(false);
    semesterRepository.save(semester);
  }
}
