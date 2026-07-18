package com.lifetracker.security;

import com.lifetracker.config.AppProperties;
import com.lifetracker.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        var user = userService.upsertFromGoogle(oauthUser);
        String token = jwtService.createToken(user.getId(), user.getEmail(), user.getName());

        ResponseCookie cookie = ResponseCookie.from(appProperties.jwt().cookieName(), token)
                .httpOnly(true)
                .secure(appProperties.cookie().secure())
                .path("/")
                .maxAge(Duration.ofDays(appProperties.jwt().expirationDays()))
                .sameSite(appProperties.cookie().sameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.sendRedirect(appProperties.frontendUrl() + "/app");
    }
}
