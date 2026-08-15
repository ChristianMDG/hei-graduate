package com.heigraduate.app.graduate.repository;

import com.heigraduate.app.graduate.model.Course;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {

  Optional<Course> findByCourseReference(String courseReference);

  boolean existsByCourseReference(String courseReference);
}
