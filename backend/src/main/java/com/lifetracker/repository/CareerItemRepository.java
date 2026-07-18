package com.lifetracker.repository;

import com.lifetracker.domain.CareerItem;
import com.lifetracker.domain.CareerItemType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CareerItemRepository extends MongoRepository<CareerItem, String> {
    List<CareerItem> findByUserIdAndTypeOrderByPriorityAscCreatedAtDesc(String userId, CareerItemType type);

    List<CareerItem> findByUserIdOrderByCreatedAtDesc(String userId);
}
