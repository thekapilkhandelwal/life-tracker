package com.lifetracker.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@Document("career_items")
public class CareerItem {
    @Id
    private String id;
    @Indexed
    private String userId;
    private CareerItemType type;
    private String title;
    private String description;
    private String status;
    private Integer priority;
    private LocalDate dueDate;
    private boolean completed;
    private Instant createdAt;
    private Instant updatedAt;
}
