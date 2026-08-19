package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "diploma_list")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class DiplomaList {

  @Id @GeneratedValue private UUID id;

  @Column(name = "promotion_id", nullable = false)
  private UUID promotionId;

  @Column(name = "parcours_id", nullable = false)
  private UUID parcoursId;

  @Column(name = "generated_at", nullable = false)
  @Builder.Default
  private LocalDateTime generatedAt = LocalDateTime.now();

  @Column(name = "url_s3", nullable = false, length = 500)
  private String urlS3;
}
