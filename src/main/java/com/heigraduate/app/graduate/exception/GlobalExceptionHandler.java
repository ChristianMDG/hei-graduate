package com.heigraduate.app.graduate.exception;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DuplicateCourseReferenceException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateCourseReference(
      DuplicateCourseReferenceException ex) {
    return badRequest(ex.getMessage());
  }

  @ExceptionHandler(CourseNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleCourseNotFound(CourseNotFoundException ex) {
    return notFound(ex.getMessage());
  }

  @ExceptionHandler(DuplicateParcoursCodeException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateParcoursCode(
      DuplicateParcoursCodeException ex) {
    return badRequest(ex.getMessage());
  }

  @ExceptionHandler(ParcoursNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleParcoursNotFound(ParcoursNotFoundException ex) {
    return notFound(ex.getMessage());
  }

  private ResponseEntity<Map<String, Object>> badRequest(String message) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(message));
  }

  private ResponseEntity<Map<String, Object>> notFound(String message) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(message));
  }

  private Map<String, Object> body(String message) {
    return Map.of("message", message, "timestamp", Instant.now().toString());
  }
}
