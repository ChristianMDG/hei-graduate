package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.CourseRequest;
import com.heigraduate.app.graduate.dto.CourseResponse;
import com.heigraduate.app.graduate.service.CourseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class CourseController {

  private final CourseService courseService;

  @GetMapping
  public List<CourseResponse> findAll() {
    return courseService.findAll();
  }

  @GetMapping("/{id}")
  public CourseResponse findById(@PathVariable UUID id) {
    return courseService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CourseResponse create(@Valid @RequestBody CourseRequest request) {
    return courseService.create(request);
  }

  @PutMapping("/{id}")
  public CourseResponse update(@PathVariable UUID id, @Valid @RequestBody CourseRequest request) {
    return courseService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(@PathVariable UUID id) {
    courseService.deactivate(id);
  }
}
