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

  @PostMapping("/generate")
  @PreAuthorize("hasRole('ADMIN')")
  public DiplomaExcelResponse generate(@PathVariable UUID promotionId) {
    return diplomaExcelService.generateExcel(promotionId);
  }
}
