package com.heigraduate.app.graduate.exception;

public class DuplicateParcoursCodeException extends ConflictException {

  public DuplicateParcoursCodeException(String code) {
    super("A parcours with code '" + code + "' already exists");
  }
}
