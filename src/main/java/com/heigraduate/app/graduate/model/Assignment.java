package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
    name = "assignment",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_assignment_course_teacher_year",
            columnNames = {"course_id", "teacher_id", "academic_year_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Assignment {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @ManyToOne(optional = false)
  @JoinColumn(name = "teacher_id", nullable = false)
  private Teacher teacher;

  @ManyToOne(optional = false)
  @JoinColumn(name = "academic_year_id", nullable = false)
  private AcademicYear academicYear;

  @ManyToMany
  @JoinTable(
      name = "assignment_group",
      joinColumns = @JoinColumn(name = "assignment_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  @Builder.Default
  private Set<Group> groups = new HashSet<>();
}
