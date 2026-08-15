package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.AcademicYearRequest;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.validator.AcademicYearValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final AcademicYearValidator academicYearValidator;

    @Transactional
    public AcademicYear create(AcademicYearRequest request) {
        academicYearValidator.validateDateRange(request.startDate(), request.endDate());
        AcademicYear academicYear =
                AcademicYear.builder()
                        .label(request.label().trim())
                        .startDate(request.startDate())
                        .endDate(request.endDate())
                        .level(request.level().trim().toUpperCase())
                        .build();
        return academicYearRepository.save(academicYear);
    }

    @Transactional
    public AcademicYear update(UUID id, AcademicYearRequest request) {
        AcademicYear existing = getOrThrow(id);
        academicYearValidator.validateDateRange(request.startDate(), request.endDate());
        existing.setLabel(request.label().trim());
        existing.setStartDate(request.startDate());
        existing.setEndDate(request.endDate());
        existing.setLevel(request.level().trim().toUpperCase());
        return academicYearRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public AcademicYear getById(UUID id) {
        return getOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<AcademicYear> getAll() {
        return academicYearRepository.findAll();
    }

    private AcademicYear getOrThrow(UUID id) {
        return academicYearRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear not found with id: " + id));
    }
}