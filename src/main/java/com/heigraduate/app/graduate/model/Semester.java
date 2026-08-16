package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "semester")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semester {

  @Id @GeneratedValue private UUID id;

  @Column(nullable = false)
  private String label;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Column(name = "expected_credits", nullable = false)
  @Builder.Default
  private Integer expectedCredits = 30;

  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;
}
