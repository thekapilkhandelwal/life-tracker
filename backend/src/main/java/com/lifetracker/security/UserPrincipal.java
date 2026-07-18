package com.lifetracker.security;

import lombok.Getter;
import org.springframework.security.core.AuthenticatedPrincipal;

@Getter
public class UserPrincipal implements AuthenticatedPrincipal {
    private final String id;
    private final String email;
    private final String name;

    public UserPrincipal(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    @Override
    public String getName() {
        return email;
    }
}
