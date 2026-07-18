package com.lifetracker.repository;

import com.lifetracker.domain.Reminder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface ReminderRepository extends MongoRepository<Reminder, String> {
    List<Reminder> findByUserIdOrderBySendAtAsc(String userId);

    List<Reminder> findBySentFalseAndSendAtLessThanEqual(Instant now);
}
