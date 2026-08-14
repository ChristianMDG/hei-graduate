package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.CourseRequest;
import com.heigraduate.app.graduate.dto.CourseResponse;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.repository.CourseRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;

  @Transactional(readOnly = true)
  public List<CourseResponse> findAll() {
    return courseRepository.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  public CourseResponse findById(UUID id) {
    return courseRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new NoSuchElementException("Course not found: " + id));
  }

  @Transactional
  public CourseResponse create(CourseRequest request) {
    if (courseRepository.existsByCourseReference(request.courseReference())) {
      throw new IllegalArgumentException(
          "A course with reference " + request.courseReference() + " already exists");
    }
    Course course =
        Course.builder()
            .courseReference(request.courseReference())
            .title(request.title())
            .credits(request.credits())
            .active(true)
            .build();
    return toDto(courseRepository.save(course));
  }

  @Transactional
  public CourseResponse update(UUID id, CourseRequest request) {
    Course course =
        courseRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Course not found: " + id));
    course.setCourseReference(request.courseReference());
    course.setTitle(request.title());
    course.setCredits(request.credits());
    return toDto(courseRepository.save(course));
  }

  @Transactional
  public void deactivate(UUID id) {
    Course course =
        courseRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("Course not found: " + id));
    course.setActive(false);
    courseRepository.save(course);
  }

  private CourseResponse toDto(Course course) {
    return new CourseResponse(
        course.getId(),
        course.getCourseReference(),
        course.getTitle(),
        course.getCredits(),
        course.getActive());
  }
}
