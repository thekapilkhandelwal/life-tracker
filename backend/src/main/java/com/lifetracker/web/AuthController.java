package com.lifetracker.web;

import com.lifetracker.config.AppProperties;
import com.lifetracker.domain.User;
import com.lifetracker.security.JwtService;
import com.lifetracker.security.UserPrincipal;
import com.lifetracker.service.UserService;
import com.lifetracker.web.dto.DemoLoginRequest;
import com.lifetracker.web.dto.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    @GetMapping("/me")
    public UserResponse me() {
        UserPrincipal principal = AuthSupport.currentUser();
        User user = userService.requireById(principal.getId());
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getPictureUrl());
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        return Map.of(
                "googleOAuthEnabled", appProperties.googleOAuthEnabled(),
                "demoLoginEnabled", appProperties.demoLoginEnabled(),
                "googleLoginPath", "/oauth2/authorization/google"
        );
    }

    @GetMapping("/google-url")
    public Map<String, String> googleUrl() {
        if (!appProperties.googleOAuthEnabled()) {
            throw new IllegalArgumentException(
                    "Google OAuth is not configured. Set GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, and GOOGLE_OAUTH_ENABLED=true."
            );
        }
        return Map.of("url", "/oauth2/authorization/google");
    }

    @PostMapping("/demo-login")
    public ResponseEntity<UserResponse> demoLogin(
            @Valid @RequestBody DemoLoginRequest request,
            HttpServletResponse response
    ) {
        if (!appProperties.demoLoginEnabled()) {
            throw new UnauthorizedException("Demo login is disabled");
        }
        User user = userService.upsertDemoUser(request.email(), request.name());
        attachSessionCookie(response, user);
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getPictureUrl()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(appProperties.jwt().cookieName(), "")
                .httpOnly(true)
                .secure(appProperties.cookie().secure())
                .path("/")
                .maxAge(0)
                .sameSite(appProperties.cookie().sameSite())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(Map.of("status", "logged_out"));
    }

    private void attachSessionCookie(HttpServletResponse response, User user) {
        String token = jwtService.createToken(user.getId(), user.getEmail(), user.getName());
        ResponseCookie cookie = ResponseCookie.from(appProperties.jwt().cookieName(), token)
                .httpOnly(true)
                .secure(appProperties.cookie().secure())
                .path("/")
                .maxAge(Duration.ofDays(appProperties.jwt().expirationDays()))
                .sameSite(appProperties.cookie().sameSite())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
