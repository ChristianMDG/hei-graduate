package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "course")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

  @Id @GeneratedValue private UUID id;

  @Column(name = "course_reference", unique = true, nullable = false)
  private String courseReference;

  @Column(nullable = false)
  private String title;

  @Column(name = "credits", nullable = false)
  private Integer credits;

  @Column(nullable = false)
  @Builder.Default
  private Boolean active = true;
}
