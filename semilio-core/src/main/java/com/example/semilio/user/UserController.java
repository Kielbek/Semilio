package com.example.semilio.user;

import com.example.semilio.user.request.ChangePasswordRequest;
import com.example.semilio.user.request.ProfileUpdateRequest;
import com.example.semilio.user.response.UserPublicResponse;
import com.example.semilio.user.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("/me")
    @ResponseStatus(code = HttpStatus.OK)
    public UserResponse getUser(
            final Authentication principal
    ) {
        return this.service.getUser(getUserId(principal));
    }

    @GetMapping("/public/{userId}")
    @ResponseStatus(code = HttpStatus.OK)
    public UserPublicResponse getUserById(
            final @PathVariable String userId
    ) {
        return this.service.getUserById(userId);
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void updateProfile(
            @RequestPart("request") @Valid final ProfileUpdateRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            final Authentication principal
    ) {
        this.service.updateProfileInfo(request, profileImage, principal);
    }

    @PostMapping("/me/password")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void changePassword(
            @RequestBody
            @Valid
            final ChangePasswordRequest request,
            final Authentication principal) {
        this.service.changePassword(request, getUserId(principal));
    }

    @PatchMapping("/me/deactivate")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deactivateAccount(final Authentication principal) {
        this.service.deactivateAccount(getUserId(principal));
    }

    @PatchMapping("/me/reactivate")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void reactivateAccount(final Authentication principal) {
        this.service.reactivateAccount(getUserId(principal));
    }

    @DeleteMapping("/me")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteAccount(final Authentication principal) {
        this.service.deleteAccount(getUserId(principal));
    }

    private String getUserId(final Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }

}