package com.example.semilio.service;

import com.example.semilio.user.User;
import org.springframework.security.core.Authentication;

public interface SecurityService {

    String getCurrentUserId(Authentication authentication);

    public User getCurrentUser(Authentication authentication);
}
