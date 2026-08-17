package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.service.TranscriptService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transcripts")
@RequiredArgsConstructor
public class TranscriptController {

  private final TranscriptService transcriptService;

  @GetMapping("/{studentId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<byte[]> generate(
      @PathVariable UUID studentId, @RequestParam UUID academicYearId) {
    byte[] pdf = transcriptService.generateTranscript(studentId, academicYearId);

    ContentDisposition disposition =
        ContentDisposition.attachment().filename("releve-" + studentId + ".pdf").build();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(disposition);

    return ResponseEntity.ok().headers(headers).body(pdf);
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<byte[]> generateMine(@AuthenticationPrincipal User connectedUser) {
    byte[] pdf = transcriptService.generateTranscriptForUser(connectedUser.getId());

    ContentDisposition disposition =
        ContentDisposition.attachment().filename("mon-releve.pdf").build();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(disposition);

    return ResponseEntity.ok().headers(headers).body(pdf);
  }
}
