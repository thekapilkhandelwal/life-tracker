package com.lifetracker.service;

import com.lifetracker.domain.User;
import com.lifetracker.repository.UserRepository;
import com.lifetracker.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User upsertFromGoogle(OAuth2User oauthUser) {
        String email = Optional.ofNullable(oauthUser.getAttribute("email"))
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException("Google account has no email"));
        String name = Optional.ofNullable(oauthUser.getAttribute("name")).map(Object::toString).orElse(email);
        String picture = Optional.ofNullable(oauthUser.getAttribute("picture")).map(Object::toString).orElse(null);
        String googleId = Optional.ofNullable(oauthUser.getAttribute("sub")).map(Object::toString).orElse(email);

        Instant now = Instant.now();
        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByGoogleId(googleId))
                .map(existing -> {
                    existing.setName(name);
                    existing.setPictureUrl(picture);
                    existing.setGoogleId(googleId);
                    existing.setLastLoginAt(now);
                    return existing;
                })
                .orElseGet(() -> User.builder()
                        .email(email)
                        .name(name)
                        .pictureUrl(picture)
                        .googleId(googleId)
                        .createdAt(now)
                        .lastLoginAt(now)
                        .build());

        return userRepository.save(user);
    }

    public User upsertDemoUser(String email, String name) {
        Instant now = Instant.now();
        User user = userRepository.findByEmail(email)
                .map(existing -> {
                    existing.setName(name);
                    existing.setLastLoginAt(now);
                    return existing;
                })
                .orElseGet(() -> User.builder()
                        .email(email)
                        .name(name)
                        .googleId("demo-" + email)
                        .createdAt(now)
                        .lastLoginAt(now)
                        .build());
        return userRepository.save(user);
    }

    public User requireById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
