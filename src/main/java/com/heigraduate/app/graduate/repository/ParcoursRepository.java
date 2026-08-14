package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Parcours;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcoursRepository extends JpaRepository<Parcours, UUID> {

  Optional<Parcours> findByCodeIgnoreCase(String code);

  boolean existsByCodeIgnoreCase(String code);

  List<Parcours> findByActiveTrue();
}
