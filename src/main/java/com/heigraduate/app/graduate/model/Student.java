package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "student")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Student {

  @Id @GeneratedValue private UUID id;

  @Column(name = "user_id", nullable = false, unique = true)
  private UUID userId;

  @Column(name = "student_number", nullable = false, unique = true, length = 20)
  private String studentNumber;

  @Column(name = "last_name", nullable = false)
  private String lastName;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "enrollment_date", nullable = false)
  private LocalDate enrollmentDate;

  @Column(nullable = false)
  private String status;
}
