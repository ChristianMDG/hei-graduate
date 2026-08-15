package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.EnrollmentRequest;
import com.heigraduate.app.graduate.dto.EnrollmentResponse;
import com.heigraduate.app.graduate.dto.TransferRequest;
import com.heigraduate.app.graduate.mapper.EnrollmentMapper;
import com.heigraduate.app.graduate.model.Enrollment;
import com.heigraduate.app.graduate.service.EnrollmentService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

  private final EnrollmentService enrollmentService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
    Enrollment created = enrollmentService.enroll(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(EnrollmentMapper.toResponse(created));
  }

  @PostMapping("/{studentId}/transfer")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<EnrollmentResponse> transfer(
      @PathVariable UUID studentId, @Valid @RequestBody TransferRequest request) {
    Enrollment updated = enrollmentService.transfer(studentId, request);
    return ResponseEntity.ok(EnrollmentMapper.toResponse(updated));
  }

  @GetMapping("/{studentId}/current")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public ResponseEntity<EnrollmentResponse> getCurrent(@PathVariable UUID studentId) {
    return ResponseEntity.ok(EnrollmentMapper.toResponse(enrollmentService.getCurrent(studentId)));
  }

  @GetMapping("/{studentId}/history")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public ResponseEntity<List<EnrollmentResponse>> getHistory(@PathVariable UUID studentId) {
    List<EnrollmentResponse> body =
        enrollmentService.getHistory(studentId).stream().map(EnrollmentMapper::toResponse).toList();
    return ResponseEntity.ok(body);
  }

  @GetMapping("/{studentId}/at")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public ResponseEntity<EnrollmentResponse> getActiveAt(
      @PathVariable UUID studentId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(
        EnrollmentMapper.toResponse(enrollmentService.getActiveAt(studentId, date)));
  }
}
