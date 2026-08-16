package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.CourseTrack;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseTrackRepository extends JpaRepository<CourseTrack, UUID> {

  List<CourseTrack> findByTrackIdAndAcademicYearId(UUID trackId, UUID academicYearId);

    List<CourseTrack> findByTrackIdAndAcademicYearIdAndMandatoryTrue(
      UUID trackId, UUID academicYearId);

  boolean existsByCourseIdAndTrackIdAndAcademicYearId(
      UUID courseId, UUID trackId, UUID academicYearId);
}
