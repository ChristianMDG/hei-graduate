package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Exam;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

  List<Exam> findByCourseIdAndAcademicYearId(UUID courseId, UUID academicYearId);
}
