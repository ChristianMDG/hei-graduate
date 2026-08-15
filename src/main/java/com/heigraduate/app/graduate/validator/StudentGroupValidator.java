package com.heigraduate.app.graduate.validator;

import com.heigraduate.app.graduate.exception.ConflictException;
import com.heigraduate.app.graduate.model.StudentGroup;
import com.heigraduate.app.graduate.repository.StudentGroupRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentGroupValidator {

    private final StudentGroupRepository studentGroupRepository;

    public void validateReferenceUniqueness(String reference, UUID excludedId) {
        Optional<StudentGroup> existing = studentGroupRepository.findByReferenceIgnoreCase(reference.trim());
        if (existing.isPresent() && !existing.get().getId().equals(excludedId)) {
            throw new ConflictException("Group with reference " + reference + " already exists");
        }
    }
}