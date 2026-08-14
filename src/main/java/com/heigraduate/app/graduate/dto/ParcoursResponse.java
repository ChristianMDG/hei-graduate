package com.heigraduate.app.graduate.dto;

import java.util.UUID;

public record ParcoursResponse(UUID id, String code, String label, boolean active) {}