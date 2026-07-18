package com.lifetracker.web.dto;

public record UserResponse(
        String id,
        String email,
        String name,
        String pictureUrl
) {
}
