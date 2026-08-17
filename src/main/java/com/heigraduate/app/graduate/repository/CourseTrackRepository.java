package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.CourseTrack;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseTrackRepository extends JpaRepository<CourseTrack, UUID> {

  /**
   * MCD v2: course_track is now keyed by semester, not academic year. These two queries stay keyed
   * by academicYearId for callers (AcademicAverageService, GraduationService via
   * CourseRequirementQuery) that reason at the year level - they just traverse
   * course_track.semester.academic_year underneath, aggregating every semester of that year.
   */
  @Query(
      "SELECT ct FROM CourseTrack ct WHERE ct.track.id = :trackId "
          + "AND ct.semester.academicYear.id = :academicYearId")
  List<CourseTrack> findByTrackIdAndAcademicYearId(
      @Param("trackId") UUID trackId, @Param("academicYearId") UUID academicYearId);

  @Query(
      "SELECT ct FROM CourseTrack ct WHERE ct.track.id = :trackId "
          + "AND ct.semester.academicYear.id = :academicYearId AND ct.mandatory = true")
  List<CourseTrack> findByTrackIdAndAcademicYearIdAndMandatoryTrue(
      @Param("trackId") UUID trackId, @Param("academicYearId") UUID academicYearId);

  List<CourseTrack> findByTrackIdAndSemesterId(UUID trackId, UUID semesterId);

  boolean existsByCourseIdAndTrackIdAndSemesterId(UUID courseId, UUID trackId, UUID semesterId);
}
