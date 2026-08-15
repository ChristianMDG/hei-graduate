package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.TeacherResponse;
import com.heigraduate.app.graduate.model.Teacher;

public final class TeacherMapper {

    private TeacherMapper() {}

    public static TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getUserId(),
                teacher.getLastName(),
                teacher.getFirstName(),
                teacher.getSpecialty(),
                teacher.getContractType());
    }
}