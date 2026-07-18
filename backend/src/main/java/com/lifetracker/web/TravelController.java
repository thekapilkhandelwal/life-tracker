package com.lifetracker.web;

import com.lifetracker.domain.TravelPage;
import com.lifetracker.service.TravelService;
import com.lifetracker.web.dto.TravelPageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/travel")
@RequiredArgsConstructor
public class TravelController {

    private final TravelService travelService;

    @GetMapping("/pages")
    public List<TravelPage> list() {
        return travelService.list(AuthSupport.currentUser().getId());
    }

    @GetMapping("/pages/{id}")
    public TravelPage get(@PathVariable String id) {
        return travelService.get(AuthSupport.currentUser().getId(), id);
    }

    @PostMapping("/pages")
    public TravelPage create(@Valid @RequestBody TravelPageRequest request) {
        return travelService.create(AuthSupport.currentUser().getId(), request);
    }

    @PutMapping("/pages/{id}")
    public TravelPage update(@PathVariable String id, @Valid @RequestBody TravelPageRequest request) {
        return travelService.update(AuthSupport.currentUser().getId(), id, request);
    }

    @DeleteMapping("/pages/{id}")
    public void delete(@PathVariable String id) {
        travelService.delete(AuthSupport.currentUser().getId(), id);
    }
}
