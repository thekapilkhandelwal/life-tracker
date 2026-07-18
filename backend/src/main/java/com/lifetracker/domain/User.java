package com.lifetracker.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document("users")
public class User {
    @Id
    private String id;
    @Indexed(unique = true)
    private String email;
    private String name;
    private String pictureUrl;
    private String googleId;
    private Instant createdAt;
    private Instant lastLoginAt;
}
