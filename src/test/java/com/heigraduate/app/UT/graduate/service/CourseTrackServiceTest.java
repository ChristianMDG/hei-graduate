package com.heigraduate.app.UT.graduate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.CourseTrack;
import com.heigraduate.app.graduate.model.Parcours;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.CourseTrackRepository;
import com.heigraduate.app.graduate.repository.ParcoursRepository;
import com.heigraduate.app.graduate.service.CourseTrackService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseTrackServiceTest {

  @Mock private CourseTrackRepository courseTrackRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private ParcoursRepository trackRepository;
  @Mock private AcademicYearRepository academicYearRepository;

  @InjectMocks private CourseTrackService courseTrackService;

  private Parcours el;
  private Parcours tn;
  private AcademicYear year1;
  private AcademicYear year2;
  private Course course;

  @BeforeEach
  void setUp() {
    el = Parcours.builder().id(UUID.randomUUID()).code("EL").label("PROG").active(true).build();
    tn = Parcours.builder().id(UUID.randomUUID()).code("TN").label("TN").active(true).build();

    year1 =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2024-2025")
            .startDate(LocalDate.of(2024, 9, 1))
            .endDate(LocalDate.of(2025, 6, 30))
            .level("L2")
            .build();

    year2 =
        AcademicYear.builder()
            .id(UUID.randomUUID())
            .label("2025-2026")
            .startDate(LocalDate.of(2025, 9, 1))
            .endDate(LocalDate.of(2026, 6, 30))
            .level("L2")
            .build();

    course =
        Course.builder()
            .id(UUID.randomUUID())
            .courseReference("PROG4")
            .title("Algorithmique avancee")
            .credits(8)
            .active(true)
            .build();
  }

  @Test
  void getMandatoryCourseIds_shouldNeverLeakElCourseIntoTn_evenAcrossDifferentYears() {
    CourseTrack elYear1 =
        CourseTrack.builder().course(course).track(el).academicYear(year1).mandatory(true).build();

    when(courseTrackRepository.findByTrackIdAndAcademicYearIdAndMandatoryTrue(
            el.getId(), year1.getId()))
        .thenReturn(List.of(elYear1));
    when(courseTrackRepository.findByTrackIdAndAcademicYearIdAndMandatoryTrue(
            tn.getId(), year1.getId()))
        .thenReturn(List.of());

    List<UUID> elCourseIdsYear1 =
        courseTrackService.getMandatoryCourseIds(el.getId(), year1.getId());
    List<UUID> tnCourseIdsYear1 =
        courseTrackService.getMandatoryCourseIds(tn.getId(), year1.getId());

    assertThat(elCourseIdsYear1).containsExactly(course.getId());
    assertThat(tnCourseIdsYear1).isEmpty();

    CourseTrack tnYear2 =
        CourseTrack.builder().course(course).track(tn).academicYear(year2).mandatory(true).build();

    when(courseTrackRepository.findByTrackIdAndAcademicYearIdAndMandatoryTrue(
            tn.getId(), year2.getId()))
        .thenReturn(List.of(tnYear2));
    when(courseTrackRepository.findByTrackIdAndAcademicYearIdAndMandatoryTrue(
            el.getId(), year2.getId()))
        .thenReturn(List.of());

    List<UUID> tnCourseIdsYear2 =
        courseTrackService.getMandatoryCourseIds(tn.getId(), year2.getId());
    List<UUID> elCourseIdsYear2 =
        courseTrackService.getMandatoryCourseIds(el.getId(), year2.getId());

    assertThat(tnCourseIdsYear2).containsExactly(course.getId());
    assertThat(elCourseIdsYear2).isEmpty();

    assertThat(elCourseIdsYear1).containsExactly(course.getId());
  }
}
