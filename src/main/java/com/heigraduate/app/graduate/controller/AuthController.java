package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.LoginRequest;
import com.heigraduate.app.graduate.dto.LoginResponse;
import com.heigraduate.app.graduate.mapper.UserMapper;
import com.heigraduate.app.graduate.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = userService.login(request.email(), request.password());
        return ResponseEntity.ok(new LoginResponse(result.token(), UserMapper.toResponse(result.user())));
    }
}