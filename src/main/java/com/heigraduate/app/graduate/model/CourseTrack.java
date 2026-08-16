package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(
        name = "course_track",
        uniqueConstraints =
        @UniqueConstraint(
                name = "uk_course_track_course_parcours_year",
                columnNames = {"course_id", "parcours_id", "academic_year_id"}))
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
    @JoinColumn(name = "parcours_id", nullable = false)
    private Parcours parcours;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(nullable = false)
    @Builder.Default
    private Boolean obligatoire = true;
}