package com.lifetracker.repository;

import com.lifetracker.domain.Expense;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expense, String> {
    List<Expense> findByUserIdAndSpentOnBetweenOrderBySpentOnDesc(String userId, LocalDate from, LocalDate to);

    List<Expense> findByUserIdOrderBySpentOnDesc(String userId);
}
