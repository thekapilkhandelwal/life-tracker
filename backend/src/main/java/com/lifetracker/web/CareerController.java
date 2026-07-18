package com.lifetracker.web;

import com.lifetracker.domain.CareerItem;
import com.lifetracker.domain.CareerItemType;
import com.lifetracker.service.CareerService;
import com.lifetracker.web.dto.CareerItemRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/career")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;

    @GetMapping("/items")
    public List<CareerItem> list(@RequestParam(required = false) CareerItemType type) {
        return careerService.list(AuthSupport.currentUser().getId(), type);
    }

    @PostMapping("/items")
    public CareerItem create(@Valid @RequestBody CareerItemRequest request) {
        return careerService.create(AuthSupport.currentUser().getId(), request);
    }

    @PutMapping("/items/{id}")
    public CareerItem update(@PathVariable String id, @Valid @RequestBody CareerItemRequest request) {
        return careerService.update(AuthSupport.currentUser().getId(), id, request);
    }

    @DeleteMapping("/items/{id}")
    public void delete(@PathVariable String id) {
        careerService.delete(AuthSupport.currentUser().getId(), id);
    }

    @PostMapping("/import")
    public List<CareerItem> importRows(@RequestBody List<Map<String, String>> rows) {
        return careerService.importRows(AuthSupport.currentUser().getId(), rows);
    }
}
