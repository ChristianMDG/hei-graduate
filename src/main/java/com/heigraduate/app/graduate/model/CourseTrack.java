package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
    name = "course_track",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_course_track_course_track_semester",
            columnNames = {"course_id", "track_id", "semester_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class CourseTrack {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @ManyToOne(optional = false)
  @JoinColumn(name = "track_id", nullable = false)
  private Parcours track;

  @ManyToOne(optional = false)
  @JoinColumn(name = "semester_id", nullable = false)
  private Semester semester;

  @Column(nullable = false)
  @Builder.Default
  private Boolean mandatory = true;
}
