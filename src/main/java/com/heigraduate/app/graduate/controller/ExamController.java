package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.ExamRequest;
import com.heigraduate.app.graduate.dto.ExamResponse;
import com.heigraduate.app.graduate.service.ExamService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ExamController {

  private final ExamService examService;

  @GetMapping
  public List<ExamResponse> findAll() {
    return examService.findAll();
  }

  @GetMapping("/{id}")
  public ExamResponse findById(@PathVariable UUID id) {
    return examService.findById(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ExamResponse create(@Valid @RequestBody ExamRequest request) {
    return examService.create(request);
  }

  @PutMapping("/{id}")
  public ExamResponse update(@PathVariable UUID id, @Valid @RequestBody ExamRequest request) {
    return examService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    examService.delete(id);
  }
}
