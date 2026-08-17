package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.DiplomaResponse;
import com.heigraduate.app.graduate.service.RankingService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotions/{promotionId}/parcours/{parcoursId}/diplomas")
@RequiredArgsConstructor
public class RankingController {

  private final RankingService rankingService;

  @PostMapping("/generate")
  @PreAuthorize("hasRole('ADMIN')")
  public List<DiplomaResponse> generate(
      @PathVariable UUID promotionId, @PathVariable UUID parcoursId) {
    return rankingService.generateRanking(promotionId, parcoursId);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public List<DiplomaResponse> getRanking(
      @PathVariable UUID promotionId, @PathVariable UUID parcoursId) {
    return rankingService.getRanking(promotionId, parcoursId);
  }
}
