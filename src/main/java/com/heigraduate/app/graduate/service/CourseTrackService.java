package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.CourseTrackRequest;
import com.heigraduate.app.graduate.dto.CourseTrackResponse;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.mapper.CourseTrackMapper;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.CourseTrack;
import com.heigraduate.app.graduate.model.Parcours;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.CourseTrackRepository;
import com.heigraduate.app.graduate.repository.ParcoursRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseTrackService {

  private final CourseTrackRepository courseTrackRepository;
  private final CourseRepository courseRepository;
  private final ParcoursRepository trackRepository;
  private final AcademicYearRepository academicYearRepository;

  @Transactional(readOnly = true)
  public List<CourseTrackResponse> findAll() {
    return courseTrackRepository.findAll().stream().map(CourseTrackMapper::toResponse).toList();
  }

  @Transactional
  public CourseTrackResponse create(CourseTrackRequest request) {
    if (courseTrackRepository.existsByCourseIdAndTrackIdAndAcademicYearId(
        request.courseId(), request.trackId(), request.academicYearId())) {
      throw new ConflictException(
          "This course is already linked to this track for this academic year");
    }

    Course course =
        courseRepository
            .findById(request.courseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Course not found with id: " + request.courseId()));
    Parcours track =
        trackRepository
            .findById(request.trackId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Track not found with id: " + request.trackId()));
    AcademicYear academicYear =
        academicYearRepository
            .findById(request.academicYearId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "AcademicYear not found with id: " + request.academicYearId()));

    CourseTrack courseTrack =
        CourseTrack.builder()
            .course(course)
            .track(track)
            .academicYear(academicYear)
            .mandatory(request.mandatory())
            .build();

    return CourseTrackMapper.toResponse(courseTrackRepository.save(courseTrack));
  }

  @Transactional
  public void delete(UUID id) {
    if (!courseTrackRepository.existsById(id)) {
      throw new ResourceNotFoundException("CourseTrack not found with id: " + id);
    }
    courseTrackRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public List<Course> getMandatoryCourses(UUID trackId, UUID academicYearId) {
    return courseTrackRepository
            .findByTrackIdAndAcademicYearIdAndMandatoryTrue(trackId, academicYearId)
        .stream()
        .map(CourseTrack::getCourse)
        .toList();
  }
}
