package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.StudentRequest;
import com.heigraduate.app.graduate.dto.StudentResponse;
import com.heigraduate.app.graduate.mapper.StudentMapper;
import com.heigraduate.app.graduate.model.Student;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.service.StudentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
    Student created = studentService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(StudentMapper.toResponse(created));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<StudentResponse> update(
      @PathVariable UUID id, @Valid @RequestBody StudentRequest request) {
    return ResponseEntity.ok(StudentMapper.toResponse(studentService.update(id, request)));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public ResponseEntity<StudentResponse> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(StudentMapper.toResponse(studentService.getById(id)));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
  public ResponseEntity<List<StudentResponse>> getAll() {
    List<StudentResponse> body =
        studentService.getAll().stream().map(StudentMapper::toResponse).toList();
    return ResponseEntity.ok(body);
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<StudentResponse> getCurrentStudent(
      @AuthenticationPrincipal User connectedUser) {
    Student self = studentService.getByCurrentUser(connectedUser.getId());
    return ResponseEntity.ok(StudentMapper.toResponse(self));
  }
}
