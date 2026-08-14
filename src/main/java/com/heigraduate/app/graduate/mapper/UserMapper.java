package com.heigraduate.app.graduate.mapper;


import com.heigraduate.app.graduate.dto.UserResponse;
import com.heigraduate.app.graduate.model.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getEmail(), user.getRole(), user.isActive(), user.getCreatedAt(), user.getLastLogin());
    }
}