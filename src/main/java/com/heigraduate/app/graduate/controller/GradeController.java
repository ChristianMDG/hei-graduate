package com.heigraduate.app.graduate.controller;

import com.heigraduate.app.graduate.dto.GradeHistoryResponse;
import com.heigraduate.app.graduate.dto.GradeRequest;
import com.heigraduate.app.graduate.dto.GradeResponse;
import com.heigraduate.app.graduate.dto.GradeUpdateRequest;
import com.heigraduate.app.graduate.model.User;
import com.heigraduate.app.graduate.service.GradeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

  private final GradeService gradeService;

  @GetMapping
  // BUG-08 FIX: un TEACHER ne doit accéder qu'aux notes de ses propres cours (§6/§18).
  // La liste complète de toutes les notes n'est accessible qu'à l'ADMIN.
  @PreAuthorize("hasRole('ADMIN')")
  public List<GradeResponse> findAll() {
    return gradeService.findAll();
  }

  @GetMapping("/{id}")
  // BUG-08 FIX: accès à une note arbitraire par UUID restreint à ADMIN.
  // Un enseignant consulte les notes via /api/grades/student/{studentId}
  // uniquement pour les étudiants de ses propres cours (vérification côté service).
  @PreAuthorize("hasRole('ADMIN')")
  public GradeResponse findById(@PathVariable UUID id) {
    return gradeService.findById(id);
  }

  /**
   * BUG-08 FIX — Route dédiée pour qu'un enseignant consulte les notes d'un étudiant donné. La
   * vérification d'affectation est faite côté service (§6/§18) : un enseignant ne voit que les
   * notes des cours auxquels il est affecté.
   */
  @GetMapping("/student/{studentId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public List<GradeResponse> findByStudent(
      @PathVariable UUID studentId, @AuthenticationPrincipal User connectedUser) {
    return gradeService.findGradesForStudent(studentId, connectedUser);
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('STUDENT')")
  public List<GradeResponse> findMyPublishedGrades(@AuthenticationPrincipal User connectedUser) {
    return gradeService.findMyPublishedGrades(connectedUser.getId());
  }

  @GetMapping("/{id}/history")
  // BUG-12 FIX: l'historique complet des modifications est réservé à l'ADMIN.
  // Un enseignant ne doit pas consulter l'historique de modifications de notes
  // d'un cours qu'il n'enseigne pas (§6/§18).
  @PreAuthorize("hasRole('ADMIN')")
  public List<GradeHistoryResponse> getHistory(@PathVariable UUID id) {
    return gradeService.getHistory(id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  @ResponseStatus(HttpStatus.CREATED)
  public GradeResponse create(
      @Valid @RequestBody GradeRequest request, @AuthenticationPrincipal User connectedUser) {
    return gradeService.create(request, connectedUser);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public GradeResponse update(
      @PathVariable UUID id,
      @Valid @RequestBody GradeUpdateRequest request,
      @AuthenticationPrincipal User connectedUser) {
    return gradeService.update(id, request, connectedUser);
  }

  @PostMapping("/{id}/publish")
  // BUG-03 FIX: la publication est une action administrative (§6/§18).
  // Un enseignant peut saisir et modifier les notes de ses cours, mais seul
  // l'administrateur peut publier (rendre visibles) les notes aux étudiants.
  @PreAuthorize("hasRole('ADMIN')")
  public GradeResponse publish(@PathVariable UUID id) {
    return gradeService.publish(id);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    gradeService.delete(id);
  }
}
