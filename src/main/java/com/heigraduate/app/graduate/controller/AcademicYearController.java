package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.AcademicYearRequest;
import com.heigraduate.app.graduate.dto.AcademicYearResponse;
import com.heigraduate.app.graduate.mapper.AcademicYearMapper;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.service.AcademicYearService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/academic-years")
@RequiredArgsConstructor
public class AcademicYearController {

  private final AcademicYearService academicYearService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AcademicYearResponse> create(
      @Valid @RequestBody AcademicYearRequest request) {
    AcademicYear created = academicYearService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(AcademicYearMapper.toResponse(created));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AcademicYearResponse> update(
      @PathVariable UUID id, @Valid @RequestBody AcademicYearRequest request) {
    return ResponseEntity.ok(
        AcademicYearMapper.toResponse(academicYearService.update(id, request)));
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<AcademicYearResponse> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(AcademicYearMapper.toResponse(academicYearService.getById(id)));
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<AcademicYearResponse>> getAll() {
    List<AcademicYearResponse> body =
        academicYearService.getAll().stream().map(AcademicYearMapper::toResponse).toList();
    return ResponseEntity.ok(body);
  }
}
