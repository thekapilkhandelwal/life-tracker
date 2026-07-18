package com.lifetracker.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@Document("reminders")
public class Reminder {
    @Id
    private String id;
    @Indexed
    private String userId;
    private String subject;
    private String body;
    private Instant sendAt;
    private boolean sent;
    private Instant createdAt;
    private Instant sentAt;
    private String errorMessage;
}
