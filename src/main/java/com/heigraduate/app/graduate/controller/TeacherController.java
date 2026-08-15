package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.TeacherRequest;
import com.heigraduate.app.graduate.dto.TeacherResponse;
import com.heigraduate.app.graduate.mapper.TeacherMapper;
import com.heigraduate.app.graduate.model.Teacher;
import com.heigraduate.app.graduate.service.TeacherService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> create(@Valid @RequestBody TeacherRequest request) {
        Teacher created = teacherService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(TeacherMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> update(
            @PathVariable UUID id, @Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(TeacherMapper.toResponse(teacherService.update(id, request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TeacherResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(TeacherMapper.toResponse(teacherService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TeacherResponse>> getAll() {
        List<TeacherResponse> body =
                teacherService.getAll().stream().map(TeacherMapper::toResponse).toList();
        return ResponseEntity.ok(body);
    }
}