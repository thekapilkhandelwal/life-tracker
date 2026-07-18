package com.lifetracker.service;

import com.lifetracker.domain.CareerItem;
import com.lifetracker.domain.CareerItemType;
import com.lifetracker.repository.CareerItemRepository;
import com.lifetracker.web.NotFoundException;
import com.lifetracker.web.dto.CareerItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CareerService {

    private final CareerItemRepository careerItemRepository;

    public List<CareerItem> list(String userId, CareerItemType type) {
        if (type == null) {
            return careerItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }
        return careerItemRepository.findByUserIdAndTypeOrderByPriorityAscCreatedAtDesc(userId, type);
    }

    public CareerItem create(String userId, CareerItemRequest request) {
        Instant now = Instant.now();
        CareerItem item = CareerItem.builder()
                .userId(userId)
                .type(request.type())
                .title(request.title())
                .description(request.description())
                .status(request.status() == null ? "OPEN" : request.status())
                .priority(request.priority() == null ? 3 : request.priority())
                .dueDate(request.dueDate())
                .completed(Boolean.TRUE.equals(request.completed()))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return careerItemRepository.save(item);
    }

    public CareerItem update(String userId, String id, CareerItemRequest request) {
        CareerItem item = requireOwned(userId, id);
        item.setType(request.type());
        item.setTitle(request.title());
        item.setDescription(request.description());
        item.setStatus(request.status() == null ? item.getStatus() : request.status());
        item.setPriority(request.priority() == null ? item.getPriority() : request.priority());
        item.setDueDate(request.dueDate());
        if (request.completed() != null) {
            item.setCompleted(request.completed());
        }
        item.setUpdatedAt(Instant.now());
        return careerItemRepository.save(item);
    }

    public void delete(String userId, String id) {
        careerItemRepository.delete(requireOwned(userId, id));
    }

    public List<CareerItem> importRows(String userId, List<Map<String, String>> rows) {
        List<CareerItem> created = new ArrayList<>();
        for (Map<String, String> row : rows) {
            String title = firstNonBlank(row, "title", "task", "name");
            if (title == null) {
                continue;
            }
            CareerItemType type = parseType(firstNonBlank(row, "type", "category"));
            CareerItemRequest request = new CareerItemRequest(
                    type,
                    title,
                    firstNonBlank(row, "description", "notes", "detail"),
                    firstNonBlank(row, "status"),
                    parsePriority(firstNonBlank(row, "priority")),
                    null,
                    "true".equalsIgnoreCase(String.valueOf(firstNonBlank(row, "completed", "done")))
            );
            created.add(create(userId, request));
        }
        return created;
    }

    private CareerItem requireOwned(String userId, String id) {
        return careerItemRepository.findById(id)
                .filter(item -> userId.equals(item.getUserId()))
                .orElseThrow(() -> new NotFoundException("Career item not found"));
    }

    private CareerItemType parseType(String raw) {
        if (raw == null || raw.isBlank()) {
            return CareerItemType.TODO;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "GOAL", "GOALS" -> CareerItemType.GOAL;
            case "LEARN", "LEARNING", "STUDY" -> CareerItemType.LEARN;
            default -> CareerItemType.TODO;
        };
    }

    private Integer parsePriority(String raw) {
        if (raw == null || raw.isBlank()) {
            return 3;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ex) {
            return 3;
        }
    }

    private String firstNonBlank(Map<String, String> row, String... keys) {
        for (String key : keys) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                if (entry.getKey() != null
                        && entry.getKey().trim().equalsIgnoreCase(key)
                        && entry.getValue() != null
                        && !entry.getValue().isBlank()) {
                    return entry.getValue().trim();
                }
            }
        }
        return null;
    }
}
