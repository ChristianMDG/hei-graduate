package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.GradeHistoryResponse;
import com.heigraduate.app.graduate.dto.GradeRequest;
import com.heigraduate.app.graduate.dto.GradeResponse;
import com.heigraduate.app.graduate.dto.GradeUpdateRequest;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.service.GradeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

  private final GradeService gradeService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public List<GradeResponse> findAll() {
    return gradeService.findAll();
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public GradeResponse findById(@PathVariable UUID id) {
    return gradeService.findById(id);
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('STUDENT')")
  public List<GradeResponse> findMyPublishedGrades(@AuthenticationPrincipal User connectedUser) {
    return gradeService.findMyPublishedGrades(connectedUser.getId());
  }

  @GetMapping("/{id}/history")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public List<GradeHistoryResponse> getHistory(@PathVariable UUID id) {
    return gradeService.getHistory(id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  @ResponseStatus(HttpStatus.CREATED)
  public GradeResponse create(
      @Valid @RequestBody GradeRequest request, @AuthenticationPrincipal User connectedUser) {
    return gradeService.create(request, connectedUser);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public GradeResponse update(
      @PathVariable UUID id,
      @Valid @RequestBody GradeUpdateRequest request,
      @AuthenticationPrincipal User connectedUser) {
    return gradeService.update(id, request, connectedUser);
  }

  @PostMapping("/{id}/publish")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public GradeResponse publish(@PathVariable UUID id) {
    return gradeService.publish(id);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    gradeService.delete(id);
  }
}
