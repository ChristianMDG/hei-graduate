package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.DiplomaExcelResponse;
import com.heigraduate.app.graduate.service.DiplomaExcelService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotions/{promotionId}/diplomas/excel")
@RequiredArgsConstructor
public class DiplomaExcelController {

  private final DiplomaExcelService diplomaExcelService;

  /**
   * Génère le fichier Excel des diplômés. Sans parcoursId : tous les parcours (EL + TN) dans un
   * seul fichier. Avec parcoursId : uniquement le parcours demandé.
   */
  @PostMapping("/generate")
  @PreAuthorize("hasRole('ADMIN')")
  public DiplomaExcelResponse generate(
      @PathVariable UUID promotionId, @RequestParam(required = false) UUID parcoursId) {
    if (parcoursId != null) {
      return diplomaExcelService.generateExcelForParcours(promotionId, parcoursId);
    }
    return diplomaExcelService.generateExcel(promotionId);
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  public DiplomaExcelResponse getExisting(
      @PathVariable UUID promotionId, @RequestParam(required = false) UUID parcoursId) {
    if (parcoursId != null) {
      return diplomaExcelService.getExistingExcelForParcours(promotionId, parcoursId);
    }
    return diplomaExcelService.getExistingExcel(promotionId);
  }
}
