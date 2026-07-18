package com.lifetracker.repository;

import com.lifetracker.domain.FinanceProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FinanceProfileRepository extends MongoRepository<FinanceProfile, String> {
    Optional<FinanceProfile> findByUserId(String userId);
}
