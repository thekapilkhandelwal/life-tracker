package com.lifetracker.web;

import com.lifetracker.domain.Reminder;
import com.lifetracker.service.ReminderService;
import com.lifetracker.web.dto.ReminderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    public List<Reminder> list() {
        return reminderService.list(AuthSupport.currentUser().getId());
    }

    @PostMapping
    public Reminder create(@Valid @RequestBody ReminderRequest request) {
        return reminderService.create(AuthSupport.currentUser().getId(), request);
    }

    @PostMapping("/{id}/send-now")
    public Reminder sendNow(@PathVariable String id) {
        return reminderService.sendNow(AuthSupport.currentUser().getId(), id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        reminderService.delete(AuthSupport.currentUser().getId(), id);
    }
}
