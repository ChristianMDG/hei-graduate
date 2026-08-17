package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.endpoint.event.EventProducer;
import com.heigraduate.app.endpoint.event.model.TranscriptEmailRequested;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.repository.StudentRepository;
import com.heigraduate.app.graduate.service.TranscriptService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
  private final StudentRepository studentRepository;
  private final EventProducer<TranscriptEmailRequested> eventProducer;

  @GetMapping("/{studentId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<byte[]> generate(
      @PathVariable UUID studentId, @RequestParam UUID academicYearId) {
    byte[] pdf = transcriptService.generateTranscript(studentId, academicYearId);
    return buildPdfResponse(pdf, "releve-" + studentId + ".pdf");
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('STUDENT')")
  public ResponseEntity<byte[]> generateMine(@AuthenticationPrincipal User connectedUser) {
    byte[] pdf = transcriptService.generateTranscriptForUser(connectedUser.getId());
    return buildPdfResponse(pdf, "mon-releve.pdf");
  }

  @PostMapping("/{studentId}/send-email")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void sendByEmail(@PathVariable UUID studentId, @RequestParam UUID academicYearId) {
    var event =
        TranscriptEmailRequested.builder()
            .studentId(studentId)
            .academicYearId(academicYearId)
            .build();
    eventProducer.accept(List.of(event));
  }

  @PostMapping("/me/send-email")
  @PreAuthorize("hasRole('STUDENT')")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void sendMineByEmail(
      @AuthenticationPrincipal User connectedUser, @RequestParam UUID academicYearId) {
    var student =
        studentRepository
            .findByUserId(connectedUser.getId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "No student profile linked to user: " + connectedUser.getId()));

    var event =
        TranscriptEmailRequested.builder()
            .studentId(student.getId())
            .academicYearId(academicYearId)
            .build();
    eventProducer.accept(List.of(event));
  }

  private ResponseEntity<byte[]> buildPdfResponse(byte[] pdf, String filename) {
    ContentDisposition disposition = ContentDisposition.attachment().filename(filename).build();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(disposition);
    return ResponseEntity.ok().headers(headers).body(pdf);
  }
}
