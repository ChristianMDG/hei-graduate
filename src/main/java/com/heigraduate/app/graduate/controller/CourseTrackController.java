package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.CourseTrackRequest;
import com.heigraduate.app.graduate.dto.CourseTrackResponse;
import com.heigraduate.app.graduate.service.CourseTrackService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/course-tracks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CourseTrackController {

    private final CourseTrackService courseTrackService;

    @GetMapping
    public List<CourseTrackResponse> findAll() {
        return courseTrackService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseTrackResponse create(@Valid @RequestBody CourseTrackRequest request) {
        return courseTrackService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        courseTrackService.delete(id);
    }
}