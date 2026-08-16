package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.AssignmentRequest;
import com.heigraduate.app.graduate.dto.AssignmentResponse;
import com.heigraduate.app.graduate.service.AssignmentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AssignmentController {

  private final AssignmentService assignmentService;

  @GetMapping
  public List<AssignmentResponse> findAll() {
    return assignmentService.findAll();
  }

  @GetMapping("/{id}")
  public AssignmentResponse findById(@PathVariable UUID id) {
    return assignmentService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AssignmentResponse create(@Valid @RequestBody AssignmentRequest request) {
    return assignmentService.create(request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    assignmentService.delete(id);
  }
}
