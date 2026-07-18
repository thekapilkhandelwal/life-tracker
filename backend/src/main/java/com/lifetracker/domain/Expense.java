package com.lifetracker.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Data
@Builder
@Document("expenses")
public class Expense {
    @Id
    private String id;
    @Indexed
    private String userId;
    private String title;
    private BigDecimal amount;
    private ExpenseCategory category;
    private String note;
    private LocalDate spentOn;
    private Instant createdAt;
}
