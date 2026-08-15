package com.heigraduate.app.graduate.exception;

public class DuplicateCourseReferenceException extends RuntimeException {

  public DuplicateCourseReferenceException(String courseReference) {
    super("A course with reference '" + courseReference + "' already exists");
  }
}
