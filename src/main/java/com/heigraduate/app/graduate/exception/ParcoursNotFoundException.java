package com.heigraduate.app.graduate.exception;

import java.util.UUID;

public class ParcoursNotFoundException extends RuntimeException {
    public ParcoursNotFoundException(UUID id) {
        super("Parcours not found with id: " + id);
    }
}
