package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.PromotionRequest;
import com.heigraduate.app.graduate.dto.PromotionResponse;
import com.heigraduate.app.graduate.mapper.PromotionMapper;
import com.heigraduate.app.graduate.model.Promotion;
import com.heigraduate.app.graduate.service.PromotionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

  private final PromotionService promotionService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<PromotionResponse> create(@Valid @RequestBody PromotionRequest request) {
    Promotion created = promotionService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(PromotionMapper.toResponse(created));
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<PromotionResponse> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(PromotionMapper.toResponse(promotionService.getById(id)));
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<PromotionResponse>> getAll() {
    List<PromotionResponse> body =
        promotionService.getAll().stream().map(PromotionMapper::toResponse).toList();
    return ResponseEntity.ok(body);
  }
}
