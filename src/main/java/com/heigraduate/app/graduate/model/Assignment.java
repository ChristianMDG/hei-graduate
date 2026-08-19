package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.*;

@Entity
// BUG-14 FIX: l'ancienne contrainte unique (course_id, teacher_id, academic_year_id) ne couvrait
// pas semester_id. Depuis V42_26, un enseignant peut être affecté au même cours en S1 ET en S2
// (deux lignes distinctes, deux semestres distincts) — c'est un cas LÉGITIME.
// La contrainte est maintenant définie par deux index partiels PostgreSQL dans V42_34 :
//   - uk_assignment_course_teacher_year_semester   (WHERE semester_id IS NOT NULL)
//   - uk_assignment_course_teacher_year_no_semester (WHERE semester_id IS NULL)
// Ces index partiels ne peuvent pas être exprimés par @UniqueConstraint JPA ; la validation
// d'unicité est donc déléguée à la base de données.
@Table(name = "assignment")
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

  @ManyToOne
  @JoinColumn(name = "semester_id")
  private Semester semester;

  @ManyToMany
  @JoinTable(
      name = "assignment_group",
      joinColumns = @JoinColumn(name = "assignment_id"),
      inverseJoinColumns = @JoinColumn(name = "group_id"))
  @Builder.Default
  private Set<Group> groups = new HashSet<>();
}
