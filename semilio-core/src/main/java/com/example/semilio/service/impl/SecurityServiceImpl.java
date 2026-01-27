package com.example.semilio.service.impl;

import com.example.semilio.service.SecurityService;
import com.example.semilio.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SecurityServiceImpl implements SecurityService {

    @Override
    public String getCurrentUserId(Authentication authentication) {
        return ((User) Objects.requireNonNull(authentication.getPrincipal())).getId();
    }

    @Override
    public User getCurrentUser(Authentication authentication) {
        return ((User) Objects.requireNonNull(authentication.getPrincipal()));
    }
}
