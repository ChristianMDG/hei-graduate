package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "enrollment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Enrollment {

  @Id @GeneratedValue private UUID id;

  @Column(name = "student_id", nullable = false)
  private UUID studentId;

  @Column(name = "parcours_id", nullable = false)
  private UUID parcoursId;

  @Column(name = "group_id", nullable = false)
  private UUID groupId;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;
}
