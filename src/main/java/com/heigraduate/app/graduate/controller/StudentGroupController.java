package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.StudentGroupRequest;
import com.heigraduate.app.graduate.dto.StudentGroupResponse;
import com.heigraduate.app.graduate.mapper.StudentGroupMapper;
import com.heigraduate.app.graduate.model.StudentGroup;
import com.heigraduate.app.graduate.service.StudentGroupService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student-groups")
@RequiredArgsConstructor
public class StudentGroupController {

  private final StudentGroupService studentGroupService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<StudentGroupResponse> create(
      @Valid @RequestBody StudentGroupRequest request) {
    StudentGroup created = studentGroupService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(StudentGroupMapper.toResponse(created));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<StudentGroupResponse> update(
      @PathVariable UUID id, @Valid @RequestBody StudentGroupRequest request) {
    return ResponseEntity.ok(
        StudentGroupMapper.toResponse(studentGroupService.update(id, request)));
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<StudentGroupResponse> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(StudentGroupMapper.toResponse(studentGroupService.getById(id)));
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<StudentGroupResponse>> getAll() {
    List<StudentGroupResponse> body =
        studentGroupService.getAll().stream().map(StudentGroupMapper::toResponse).toList();
    return ResponseEntity.ok(body);
  }
}
