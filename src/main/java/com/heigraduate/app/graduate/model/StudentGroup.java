package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "student_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class StudentGroup {

  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, length = 20)
  private String reference; // e.g. "K1", "K2", "K3"

  @Column(name = "max_size")
  private Integer maxSize;
}
