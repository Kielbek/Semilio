package com.example.semilio.auth;

import com.example.semilio.auth.request.*;
import com.example.semilio.auth.response.AuthenticationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid
            @RequestBody
            final AuthenticationRequest request) {
        return ResponseEntity.ok(this.service.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid
            @RequestBody
            final RegistrationRequest request) {
        this.service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @RequestBody
            final RefreshRequest req) {
        return ResponseEntity.ok(this.service.refreshToken(req));
    }

    @PostMapping("/verify-sms")
    public ResponseEntity<Void> verifySms(
            @Valid
            @RequestBody
            final SmsVerificationRequest req) {

        this.service.smsUserVerification(req);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @Valid
            @RequestBody
            final ForgotPasswordRequest req
    ) {
        this.service.forgotPassword(req);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid
            @RequestBody
            final ResetPasswordRequest req
    ) {
        this.service.resetPassword(req);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}