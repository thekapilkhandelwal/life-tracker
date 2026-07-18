package com.lifetracker.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@Document("travel_pages")
public class TravelPage {
    @Id
    private String id;
    @Indexed
    private String userId;
    private String title;
    private String icon;
    private String content;
    private List<String> tags;
    private boolean archived;
    private Instant createdAt;
    private Instant updatedAt;
}
