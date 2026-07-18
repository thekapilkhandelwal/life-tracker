package com.lifetracker.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@Document("finance_profiles")
public class FinanceProfile {
    @Id
    private String id;
    @Indexed(unique = true)
    private String userId;
    private BigDecimal monthlySalary;
    private BigDecimal idealNeedsPercent;
    private BigDecimal idealWantsPercent;
    private BigDecimal idealInvestPercent;
    private BigDecimal idealSavingsPercent;
    private BigDecimal goalAmount;
    private String goalLabel;
    private Instant updatedAt;
}
