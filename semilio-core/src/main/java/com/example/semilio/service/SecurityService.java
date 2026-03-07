package com.example.semilio.service;

import com.example.semilio.security.SecurityUser;
import com.example.semilio.user.model.User;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface SecurityService {

    UUID getCurrentUserId(Authentication authentication);

    User getCurrentUser(Authentication authentication);
}
