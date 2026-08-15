package com.heigraduate.app.graduate.dto;

import java.util.UUID;

public record StudentGroupResponse(UUID id, String reference, Integer maxSize) {}