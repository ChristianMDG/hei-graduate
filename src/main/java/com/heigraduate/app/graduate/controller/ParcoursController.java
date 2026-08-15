package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.ParcoursRequest;
import com.heigraduate.app.graduate.dto.ParcoursResponse;
import com.heigraduate.app.graduate.mapper.ParcoursMapper;
import com.heigraduate.app.graduate.model.Parcours;
import com.heigraduate.app.graduate.service.ParcoursService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parcours")
@RequiredArgsConstructor
public class ParcoursController {

    private final ParcoursService parcoursService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParcoursResponse> create(@Valid @RequestBody ParcoursRequest request) {
        Parcours created = parcoursService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ParcoursMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParcoursResponse> update(
            @PathVariable UUID id, @Valid @RequestBody ParcoursRequest request) {
        return ResponseEntity.ok(ParcoursMapper.toResponse(parcoursService.update(id, request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParcoursResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ParcoursMapper.toResponse(parcoursService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ParcoursResponse>> getAll(
            @RequestParam(defaultValue = "false") boolean activeOnly) {
        List<ParcoursResponse> body =
                parcoursService.getAll(activeOnly).stream().map(ParcoursMapper::toResponse).toList();
        return ResponseEntity.ok(body);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParcoursResponse> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ParcoursMapper.toResponse(parcoursService.deactivate(id)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ParcoursResponse> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ParcoursMapper.toResponse(parcoursService.activate(id)));
    }
}