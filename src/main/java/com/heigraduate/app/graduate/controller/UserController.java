package com.heigraduate.app.graduate.controller;


import com.heigraduate.app.graduate.dto.CreateUserRequest;
import com.heigraduate.app.graduate.dto.UpdateUserRequest;
import com.heigraduate.app.graduate.mapper.UserMapper;
import com.heigraduate.app.graduate.dto.UserResponse;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.service.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User created = userService.createUser(request.email(), request.password(), request.role());
        return ResponseEntity.created(URI.create("/api/users/" + created.getId()))
                .body(UserMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(UserMapper.toResponse(userService.getUserById(id)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAll() {
        List<UserResponse> body = userService.getAllUsers().stream().map(UserMapper::toResponse).toList();
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        User updated = userService.updateUser(id, request.email(), request.role(), request.active());
        return ResponseEntity.ok(UserMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}