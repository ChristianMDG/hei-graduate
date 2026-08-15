package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

  @Id @GeneratedValue private UUID id;

  @Column(name = "reference", unique = true, nullable = false)
  private String reference;

  @Column private Integer capacity;

  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;
}
