package com.lifetracker.repository;

import com.lifetracker.domain.TravelPage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TravelPageRepository extends MongoRepository<TravelPage, String> {
    List<TravelPage> findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(String userId);

    Optional<TravelPage> findByIdAndUserId(String id, String userId);
}
