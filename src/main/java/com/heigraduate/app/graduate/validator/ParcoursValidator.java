package com.heigraduate.app.graduate.validator;

import com.heigraduate.app.graduate.exception.DuplicateParcoursCodeException;
import com.heigraduate.app.graduate.model.Parcours;
import com.heigraduate.app.graduate.repository.ParcoursRepository;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParcoursValidator {

  private final ParcoursRepository parcoursRepository;

  public void validateCodeUniqueness(String code, UUID excludedId) {
    Optional<Parcours> existing = parcoursRepository.findByCodeIgnoreCase(code.trim());
    if (existing.isPresent() && !existing.get().getId().equals(excludedId)) {
      throw new DuplicateParcoursCodeException(code);
    }
  }
}
