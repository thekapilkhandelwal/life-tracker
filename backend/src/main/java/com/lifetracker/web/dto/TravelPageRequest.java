package com.lifetracker.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record TravelPageRequest(
        @NotBlank String title,
        String icon,
        String content,
        List<String> tags,
        Boolean archived
) {
}
