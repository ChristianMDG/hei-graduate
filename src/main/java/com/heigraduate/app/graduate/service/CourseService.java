package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.CourseRequest;
import com.heigraduate.app.graduate.dto.CourseResponse;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.mapper.CourseMapper;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.repository.CourseRepository;
import java.util.List;
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
        return courseRepository.findAll().stream().map(CourseMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(UUID id) {
        return courseRepository
                .findById(id)
                .map(CourseMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        if (courseRepository.existsByCourseReference(request.courseReference())) {
            throw new ConflictException(
                    "A course with reference '" + request.courseReference() + "' already exists");
        }
        Course course =
                Course.builder()
                        .courseReference(request.courseReference())
                        .title(request.title())
                        .credits(request.credits())
                        .active(true)
                        .build();
        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse update(UUID id, CourseRequest request) {
        Course course =
                courseRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        course.setCourseReference(request.courseReference());
        course.setTitle(request.title());
        course.setCredits(request.credits());
        return CourseMapper.toResponse(courseRepository.save(course));
    }

    @Transactional
    public void deactivate(UUID id) {
        Course course =
                courseRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        course.setActive(false);
        courseRepository.save(course);
    }
}