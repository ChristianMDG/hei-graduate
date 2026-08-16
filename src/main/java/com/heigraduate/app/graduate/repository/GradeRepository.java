package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Grade;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeRepository extends JpaRepository<Grade, UUID> {

  Optional<Grade> findByStudentIdAndExamId(UUID studentId, UUID examId);

  boolean existsByStudentIdAndExamId(UUID studentId, UUID examId);

  List<Grade> findByStudentId(UUID studentId);

  List<Grade> findByStudentIdAndStatus(
      UUID studentId, com.heigraduate.app.graduate.model.GradeStatus status);

  @org.springframework.data.jpa.repository.Query(
      "SELECT g FROM Grade g WHERE g.student.id = :studentId AND g.exam.course.id = :courseId "
          + "AND g.status = com.heigraduate.app.graduate.model.GradeStatus.PUBLISHED")
  List<Grade> findPublishedByStudentIdAndCourseId(UUID studentId, UUID courseId);
}
