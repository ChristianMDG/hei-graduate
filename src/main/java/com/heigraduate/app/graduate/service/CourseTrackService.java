package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.contract.CourseRequirementQuery;
import com.heigraduate.app.graduate.dto.CourseTrackRequest;
import com.heigraduate.app.graduate.dto.CourseTrackResponse;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.mapper.CourseTrackMapper;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.CourseTrack;
import com.heigraduate.app.graduate.model.Parcours;
import com.heigraduate.app.graduate.model.Semester;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.CourseTrackRepository;
import com.heigraduate.app.graduate.repository.ParcoursRepository;
import com.heigraduate.app.graduate.repository.SemesterRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseTrackService implements CourseRequirementQuery {

  private final CourseTrackRepository courseTrackRepository;
  private final CourseRepository courseRepository;
  private final ParcoursRepository trackRepository;
  private final SemesterRepository semesterRepository;

  @Transactional(readOnly = true)
  public List<CourseTrackResponse> findAll() {
    return courseTrackRepository.findAll().stream().map(CourseTrackMapper::toResponse).toList();
  }

  @Transactional
  public CourseTrackResponse create(CourseTrackRequest request) {
    if (courseTrackRepository.existsByCourseIdAndTrackIdAndSemesterId(
        request.courseId(), request.trackId(), request.semesterId())) {
      throw new ConflictException("This course is already linked to this track for this semester");
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
    Semester semester =
        semesterRepository
            .findById(request.semesterId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Semester not found with id: " + request.semesterId()));

    List<CourseTrack> existingTracks =
        courseTrackRepository.findByTrackIdAndSemesterId(request.trackId(), request.semesterId());
    int currentCredits = existingTracks.stream().mapToInt(ct -> ct.getCourse().getCredits()).sum();
    int newTotal = currentCredits + course.getCredits();

    if (newTotal > 30) {
      throw new com.heigraduate.app.graduate.exception.BadRequestException(
          "Adding this course ("
              + course.getCredits()
              + " ECTS) would exceed the 30 ECTS limit per semester for track "
              + track.getCode()
              + " (current: "
              + currentCredits
              + " ECTS)");
    }

    CourseTrack courseTrack =
        CourseTrack.builder()
            .course(course)
            .track(track)
            .semester(semester)
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

  @Override
  @Transactional(readOnly = true)
  public List<UUID> getMandatoryCourseIds(UUID trackId, UUID academicYearId) {
    return courseTrackRepository
        .findByTrackIdAndAcademicYearIdAndMandatoryTrue(trackId, academicYearId)
        .stream()
        .map(ct -> ct.getCourse().getId())
        .toList();
  }
}
