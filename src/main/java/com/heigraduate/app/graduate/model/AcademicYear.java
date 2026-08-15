package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "academic_year")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class AcademicYear {

  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, length = 20)
  private String label; // e.g. "2025-2026"

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(nullable = false, length = 10)
  private String level; // "L1", "L2", "L3"
}
