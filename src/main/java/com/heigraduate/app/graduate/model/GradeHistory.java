package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "grade_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class GradeHistory {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "grade_id", nullable = false)
  private Grade grade;

  @Column(name = "old_value", nullable = false, precision = 4, scale = 2)
  private BigDecimal oldValue;

  @Column(name = "new_value", nullable = false, precision = 4, scale = 2)
  private BigDecimal newValue;

  @Column(nullable = false)
  private String reason;

  @Column(name = "changed_by_user_id", nullable = false)
  private UUID changedByUserId;

  @Column(name = "changed_at", nullable = false)
  @Builder.Default
  private LocalDateTime changedAt = LocalDateTime.now();
}
