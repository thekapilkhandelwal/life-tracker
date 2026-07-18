package com.lifetracker.service;

import com.lifetracker.config.AppProperties;
import com.lifetracker.domain.Reminder;
import com.lifetracker.domain.User;
import com.lifetracker.repository.ReminderRepository;
import com.lifetracker.web.NotFoundException;
import com.lifetracker.web.dto.ReminderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserService userService;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final AppProperties appProperties;

    public List<Reminder> list(String userId) {
        return reminderRepository.findByUserIdOrderBySendAtAsc(userId);
    }

    public Reminder create(String userId, ReminderRequest request) {
        Reminder reminder = Reminder.builder()
                .userId(userId)
                .subject(request.subject())
                .body(request.body())
                .sendAt(request.sendAt())
                .sent(false)
                .createdAt(Instant.now())
                .build();
        return reminderRepository.save(reminder);
    }

    public void delete(String userId, String id) {
        Reminder reminder = reminderRepository.findById(id)
                .filter(item -> userId.equals(item.getUserId()))
                .orElseThrow(() -> new NotFoundException("Reminder not found"));
        reminderRepository.delete(reminder);
    }

    public Reminder sendNow(String userId, String id) {
        Reminder reminder = reminderRepository.findById(id)
                .filter(item -> userId.equals(item.getUserId()))
                .orElseThrow(() -> new NotFoundException("Reminder not found"));
        dispatch(reminder);
        return reminderRepository.save(reminder);
    }

    @Scheduled(fixedDelayString = "${app.reminder-poll-ms:60000}")
    public void processDueReminders() {
        reminderRepository.findBySentFalseAndSendAtLessThanEqual(Instant.now())
                .forEach(reminder -> {
                    dispatch(reminder);
                    reminderRepository.save(reminder);
                });
    }

    private void dispatch(Reminder reminder) {
        try {
            User user = userService.requireById(reminder.getUserId());
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null || appProperties.mailFrom() == null || appProperties.mailFrom().isBlank()) {
                reminder.setErrorMessage("Mail not configured. Set MAIL_USERNAME/MAIL_PASSWORD.");
                log.warn("Skipping reminder {} — mail not configured", reminder.getId());
                return;
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appProperties.mailFrom());
            message.setTo(user.getEmail());
            message.setSubject(reminder.getSubject());
            message.setText(reminder.getBody());
            mailSender.send(message);
            reminder.setSent(true);
            reminder.setSentAt(Instant.now());
            reminder.setErrorMessage(null);
        } catch (Exception ex) {
            reminder.setErrorMessage(ex.getMessage());
            log.error("Failed to send reminder {}", reminder.getId(), ex);
        }
    }
}
