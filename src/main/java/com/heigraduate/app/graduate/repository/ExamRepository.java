package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Exam;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

  List<Exam> findByCourseIdAndAcademicYearId(UUID courseId, UUID academicYearId);

  /**
   * BUG-01 FIX — Agrégation par cours + semestre (et non plus par année). Utilisé par {@code
   * ExamValidator.validateCoefficientSum} depuis la correction de BUG-01.
   */
  List<Exam> findByCourseIdAndSemesterId(UUID courseId, UUID semesterId);
}
