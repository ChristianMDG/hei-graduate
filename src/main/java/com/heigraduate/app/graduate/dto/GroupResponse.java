package com.heigraduate.app.graduate.dto;

import java.util.UUID;

public record GroupResponse(UUID id, String reference, Integer capacity, Boolean active) {}
