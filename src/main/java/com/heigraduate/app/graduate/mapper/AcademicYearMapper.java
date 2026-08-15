package com.heigraduate.app.graduate.mapper;


import com.heigraduate.app.graduate.dto.AcademicYearResponse;
import com.heigraduate.app.graduate.model.AcademicYear;

public final class AcademicYearMapper {

    private AcademicYearMapper() {}

    public static AcademicYearResponse toResponse(AcademicYear academicYear) {
        return new AcademicYearResponse(
                academicYear.getId(),
                academicYear.getLabel(),
                academicYear.getStartDate(),
                academicYear.getEndDate(),
                academicYear.getLevel());
    }
}