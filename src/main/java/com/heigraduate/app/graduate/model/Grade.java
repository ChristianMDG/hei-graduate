package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
    name = "grade",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_grade_student_exam",
            columnNames = {"student_id", "exam_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Grade {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(optional = false)
  @JoinColumn(name = "exam_id", nullable = false)
  private Exam exam;

  @Column(nullable = false, precision = 4, scale = 2)
  private BigDecimal value;

  @Column(name = "entered_by_user_id")
  private UUID enteredByUserId;

  @Column(name = "entered_at", nullable = false)
  @Builder.Default
  private LocalDateTime enteredAt = LocalDateTime.now();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private GradeStatus status = GradeStatus.DRAFT;
}
