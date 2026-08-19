package com.heigraduate.app.graduate.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "transcript")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Transcript {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(optional = false)
  @JoinColumn(name = "academic_year_id", nullable = false)
  private AcademicYear academicYear;

  @ManyToOne
  @JoinColumn(name = "semester_id")
  private Semester semester;

  @Column(nullable = false, length = 20)
  private String type;

  @Column(name = "generated_at", nullable = false)
  @Builder.Default
  private LocalDateTime generatedAt = LocalDateTime.now();

  @Column(name = "url_s3", nullable = false)
  private String urlS3;

  @Column(name = "average_grade", precision = 4, scale = 2)
  private BigDecimal averageGrade;

  @Column(name = "obtained_credits", nullable = false)
  private Integer obtainedCredits;

  @Column(name = "expected_credits", nullable = false)
  private Integer expectedCredits;

  @Column(name = "year_validated", nullable = false)
  private Boolean yearValidated;

  @Column(name = "email_sent", nullable = false)
  @Builder.Default
  private Boolean emailSent = false;

  @Column(name = "email_sent_at")
  private LocalDateTime emailSentAt;
}
