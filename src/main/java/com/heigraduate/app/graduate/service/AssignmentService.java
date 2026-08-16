package com.heigraduate.app.graduate.service;

import com.heigraduate.app.graduate.dto.AssignmentRequest;
import com.heigraduate.app.graduate.dto.AssignmentResponse;
import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.ResourceNotFoundException;
import com.heigraduate.app.graduate.mapper.AssignmentMapper;
import com.heigraduate.app.graduate.model.AcademicYear;
import com.heigraduate.app.graduate.model.Assignment;
import com.heigraduate.app.graduate.model.Course;
import com.heigraduate.app.graduate.model.Group;
import com.heigraduate.app.graduate.model.Teacher;
import com.heigraduate.app.graduate.repository.AcademicYearRepository;
import com.heigraduate.app.graduate.repository.AssignmentRepository;
import com.heigraduate.app.graduate.repository.CourseRepository;
import com.heigraduate.app.graduate.repository.GroupRepository;
import com.heigraduate.app.graduate.repository.TeacherRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignmentService {

  private final AssignmentRepository assignmentRepository;
  private final CourseRepository courseRepository;
  private final TeacherRepository teacherRepository;
  private final AcademicYearRepository academicYearRepository;
  private final GroupRepository groupRepository;

  @Transactional(readOnly = true)
  public List<AssignmentResponse> findAll() {
    return assignmentRepository.findAll().stream().map(AssignmentMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public AssignmentResponse findById(UUID id) {
    return assignmentRepository
        .findById(id)
        .map(AssignmentMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + id));
  }

  @Transactional
  public AssignmentResponse create(AssignmentRequest request) {
    if (assignmentRepository.existsByCourseIdAndTeacherIdAndAcademicYearId(
        request.courseId(), request.teacherId(), request.academicYearId())) {
      throw new ConflictException(
          "This teacher is already assigned to this course for this academic year");
    }

    Course course =
        courseRepository
            .findById(request.courseId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Course not found with id: " + request.courseId()));
    Teacher teacher =
        teacherRepository
            .findById(request.teacherId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Teacher not found with id: " + request.teacherId()));
    AcademicYear academicYear =
        academicYearRepository
            .findById(request.academicYearId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "AcademicYear not found with id: " + request.academicYearId()));

    Set<Group> groups = new HashSet<>();
    for (UUID groupId : request.groupIds()) {
      Group group =
          groupRepository
              .findById(groupId)
              .orElseThrow(
                  () -> new ResourceNotFoundException("Group not found with id: " + groupId));
      groups.add(group);
    }

    Assignment assignment =
        Assignment.builder()
            .course(course)
            .teacher(teacher)
            .academicYear(academicYear)
            .groups(groups)
            .build();

    return AssignmentMapper.toResponse(assignmentRepository.save(assignment));
  }

  @Transactional
  public void delete(UUID id) {
    if (!assignmentRepository.existsById(id)) {
      throw new ResourceNotFoundException("Assignment not found with id: " + id);
    }
    assignmentRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public boolean isTeacherAssignedToCourse(UUID teacherId, UUID courseId, UUID academicYearId) {
    return assignmentRepository.existsByCourseIdAndTeacherIdAndAcademicYearId(
        courseId, teacherId, academicYearId);
  }
}
