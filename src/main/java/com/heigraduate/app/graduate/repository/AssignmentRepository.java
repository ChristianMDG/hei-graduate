package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Assignment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {

  boolean existsByCourseIdAndTeacherIdAndAcademicYearId(
      UUID courseId, UUID teacherId, UUID academicYearId);
}
