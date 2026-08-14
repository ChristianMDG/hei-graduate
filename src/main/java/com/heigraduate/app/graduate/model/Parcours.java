package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "parcours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Parcours {

  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, unique = true, length = 10)
  private String code;

  @Column(nullable = false)
  private String label;

  @Column(nullable = false)
  private boolean active;
}
