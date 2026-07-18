package com.lifetracker.web;

import com.lifetracker.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthSupport {

    private AuthSupport() {
    }

    public static UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("Not authenticated");
        }
        return principal;
    }
}
