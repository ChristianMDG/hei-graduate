package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.SemesterRequest;
import com.heigraduate.app.graduate.dto.SemesterResponse;
import com.heigraduate.app.graduate.service.SemesterService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/semesters")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SemesterController {

  private final SemesterService semesterService;

  @GetMapping
  public List<SemesterResponse> findAll() {
    return semesterService.findAll();
  }

  @GetMapping("/{id}")
  public SemesterResponse findById(@PathVariable UUID id) {
    return semesterService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SemesterResponse create(@Valid @RequestBody SemesterRequest request) {
    return semesterService.create(request);
  }

  @PutMapping("/{id}")
  public SemesterResponse update(
      @PathVariable UUID id, @Valid @RequestBody SemesterRequest request) {
    return semesterService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(@PathVariable UUID id) {
    semesterService.deactivate(id);
  }
}
