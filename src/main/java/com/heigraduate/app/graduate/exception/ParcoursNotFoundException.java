package com.heigraduate.app.graduate.exception;

import java.util.UUID;

public class ParcoursNotFoundException extends NotFoundException {
  public ParcoursNotFoundException(UUID id) {
    super("Parcours not found with id: " + id);
  }
}
