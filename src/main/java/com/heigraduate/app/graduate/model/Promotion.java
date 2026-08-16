package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "promotion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Promotion {

    @Id @GeneratedValue private UUID id;

    @Column(nullable = false)
    private String label;

    @ManyToOne
    @JoinColumn(name = "final_academic_year_id", nullable = false)
    private AcademicYear finalAcademicYear;
}