package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.ParcoursRequest;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.model.Parcours;
import com.heigraduate.app.graduate.repository.ParcoursRepository;
import com.heigraduate.app.graduate.validator.ParcoursValidator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParcoursService {

  private final ParcoursRepository parcoursRepository;
  private final ParcoursValidator parcoursValidator;

  @Transactional
  public Parcours create(ParcoursRequest request) {
    parcoursValidator.validateCodeUniqueness(request.code(), null);
    Parcours parcours =
        Parcours.builder()
            .code(request.code().trim().toUpperCase())
            .label(request.label().trim())
            .active(true)
            .build();
    return parcoursRepository.save(parcours);
  }

  @Transactional
  public Parcours update(UUID id, ParcoursRequest request) {
    Parcours existing = getOrThrow(id);
    parcoursValidator.validateCodeUniqueness(request.code(), id);
    existing.setCode(request.code().trim().toUpperCase());
    existing.setLabel(request.label().trim());
    return parcoursRepository.save(existing);
  }

  @Transactional(readOnly = true)
  public Parcours getById(UUID id) {
    return getOrThrow(id);
  }

  @Transactional(readOnly = true)
  public List<Parcours> getAll(boolean activeOnly) {
    return activeOnly ? parcoursRepository.findByActiveTrue() : parcoursRepository.findAll();
  }

  @Transactional
  public Parcours deactivate(UUID id) {
    Parcours existing = getOrThrow(id);
    existing.setActive(false);
    return parcoursRepository.save(existing);
  }

  @Transactional
  public Parcours activate(UUID id) {
    Parcours existing = getOrThrow(id);
    existing.setActive(true);
    return parcoursRepository.save(existing);
  }

  private Parcours getOrThrow(UUID id) {
    return parcoursRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Parcours not found with id: " + id));
  }
}
