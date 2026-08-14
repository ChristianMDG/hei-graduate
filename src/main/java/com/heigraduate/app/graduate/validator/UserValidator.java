package com.heigraduate.app.graduate.validator;


import java.util.UUID;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.exception.DuplicateUserCodeException;
import com.heigraduate.app.graduate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateEmailIsUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserCodeException("A user with email '" + email + "' already exists");
        }
    }

    public void validateEmailIsUniqueForUpdate(String email, UUID currentUserId) {
        userRepository
                .findByEmail(email)
                .filter(existing -> !existing.getId().equals(currentUserId))
                .ifPresent(
                        existing -> {
                            throw new DuplicateUserCodeException("A user with email '" + email + "' already exists");
                        });
    }
}