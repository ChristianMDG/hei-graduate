package com.heigraduate.app.graduate.mapper;

import com.heigraduate.app.graduate.dto.StudentResponse;
import com.heigraduate.app.graduate.model.Student;

public final class StudentMapper {

    private StudentMapper() {}

    public static StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getUserId(),
                student.getStudentNumber(),
                student.getLastName(),
                student.getFirstName(),
                student.getBirthDate(),
                student.getEnrollmentDate(),
                student.getStatus());
    }
}