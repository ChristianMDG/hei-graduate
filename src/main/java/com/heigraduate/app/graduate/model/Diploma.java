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

  /**
   * BUG-16 NOTE — Ces trois champs utilisent des UUID bruts au lieu de {@code @ManyToOne} JPA.
   *
   * <p>L'intégrité référentielle est garantie par les clés étrangères de la base de données
   * (migration V42_22 : {@code REFERENCES student(id)}, {@code REFERENCES promotion(id)}, {@code
   * REFERENCES parcours(id)}). Une tentative d'insertion d'un UUID invalide sera rejetée par
   * PostgreSQL avec une erreur de contrainte FK.
   *
   * <p>La migration vers des relations {@code @ManyToOne} JPA est recommandée à long terme mais
   * nécessite de refactoriser {@code DiplomaRepository}, {@code RankingService}, {@code
   * DiplomaMapper} et tous leurs tests. Décision : conserver les UUID bruts pour l'instant et ne
   * pas introduire de régression.
   */
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
