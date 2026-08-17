package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.CourseTrack;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseTrackRepository extends JpaRepository<CourseTrack, UUID> {

  List<CourseTrack> findByTrackIdAndSemesterId(UUID trackId, UUID semesterId);

  List<CourseTrack> findByTrackIdAndSemesterIdAndMandatoryTrue(UUID trackId, UUID semesterId);

  boolean existsByCourseIdAndTrackIdAndSemesterId(UUID courseId, UUID trackId, UUID semesterId);

  @Query(
      "SELECT ct FROM CourseTrack ct WHERE ct.track.id = :trackId "
          + "AND ct.semester.academicYear.id = :academicYearId")
  List<CourseTrack> findByTrackIdAndAcademicYearId(UUID trackId, UUID academicYearId);

  @Query(
      "SELECT ct FROM CourseTrack ct WHERE ct.track.id = :trackId "
          + "AND ct.semester.academicYear.id = :academicYearId AND ct.mandatory = true")
  List<CourseTrack> findByTrackIdAndAcademicYearIdAndMandatoryTrue(
      UUID trackId, UUID academicYearId);
}
