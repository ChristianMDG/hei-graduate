package com.heigraduate.app.graduate.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CourseTrackRequest(
    @NotNull UUID courseId,
    @NotNull UUID trackId,
    @NotNull UUID semesterId,
    @NotNull Boolean mandatory) {}
