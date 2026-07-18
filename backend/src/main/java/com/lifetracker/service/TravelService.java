package com.lifetracker.service;

import com.lifetracker.domain.TravelPage;
import com.lifetracker.repository.TravelPageRepository;
import com.lifetracker.web.NotFoundException;
import com.lifetracker.web.dto.TravelPageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TravelService {

    private final TravelPageRepository travelPageRepository;

    public List<TravelPage> list(String userId) {
        return travelPageRepository.findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(userId);
    }

    public TravelPage get(String userId, String id) {
        return travelPageRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Travel page not found"));
    }

    public TravelPage create(String userId, TravelPageRequest request) {
        Instant now = Instant.now();
        TravelPage page = TravelPage.builder()
                .userId(userId)
                .title(request.title())
                .icon(request.icon() == null || request.icon().isBlank() ? "✈" : request.icon())
                .content(request.content() == null ? "" : request.content())
                .tags(request.tags() == null ? List.of() : request.tags())
                .archived(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return travelPageRepository.save(page);
    }

    public TravelPage update(String userId, String id, TravelPageRequest request) {
        TravelPage page = get(userId, id);
        page.setTitle(request.title());
        if (request.icon() != null) {
            page.setIcon(request.icon());
        }
        if (request.content() != null) {
            page.setContent(request.content());
        }
        if (request.tags() != null) {
            page.setTags(request.tags());
        }
        if (request.archived() != null) {
            page.setArchived(request.archived());
        }
        page.setUpdatedAt(Instant.now());
        return travelPageRepository.save(page);
    }

    public void delete(String userId, String id) {
        travelPageRepository.delete(get(userId, id));
    }
}
