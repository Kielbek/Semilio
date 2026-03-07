package com.example.semilio.user.service;

import com.example.semilio.user.request.ChangePasswordRequest;
import com.example.semilio.user.request.ProfileUpdateRequest;
import com.example.semilio.user.response.UserPublicResponse;
import com.example.semilio.user.response.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserService extends UserDetailsService {

    UserResponse getUser(UUID userId);

    void updateProfileInfo(ProfileUpdateRequest request, MultipartFile profileImage, Authentication principal);

    void changePassword(ChangePasswordRequest request, UUID userId);

    void deactivateAccount(UUID userId);

    void reactivateAccount(UUID userId);

    void deleteAccount(UUID userId);

    UserPublicResponse getUserById(UUID userId);
}