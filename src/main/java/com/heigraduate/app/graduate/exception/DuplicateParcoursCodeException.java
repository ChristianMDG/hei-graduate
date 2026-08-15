package com.heigraduate.app.graduate.exception;

public class DuplicateParcoursCodeException extends RuntimeException {

  public DuplicateParcoursCodeException(String code) {
    super("A parcours with code '" + code + "' already exists");
  }
}
