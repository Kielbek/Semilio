package com.example.semilio.user;

import com.example.semilio.user.request.ChangePasswordRequest;
import com.example.semilio.user.request.ProfileUpdateRequest;
import com.example.semilio.user.response.UserPublicResponse;
import com.example.semilio.user.response.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends UserDetailsService {

    UserResponse getUser(String userId);

    void updateProfileInfo(ProfileUpdateRequest request, MultipartFile profileImage, Authentication principal);

    void changePassword(ChangePasswordRequest request, String userId);

    void deactivateAccount(String userId);

    void reactivateAccount(String userId);

    void deleteAccount(String userId);

    UserPublicResponse getUserById(String userId);
}