package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "diploma")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Diploma {

  @Id @GeneratedValue private UUID id;

  @Column(name = "student_id", nullable = false)
  private UUID studentId;

  @Column(name = "promotion_id", nullable = false)
  private UUID promotionId;

  @Column(name = "parcours_id", nullable = false)
  private UUID parcoursId;

  @Column(name = "obtained_date", nullable = false)
  private LocalDate obtainedDate;

  @Column(name = "overall_average", nullable = false)
  private BigDecimal overallAverage;

  @Column(nullable = false)
  private Integer rank;

  @Column(nullable = false)
  private String mention;
}
