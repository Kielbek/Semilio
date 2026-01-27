package com.example.semilio.auth;

import com.example.semilio.auth.request.*;
import com.example.semilio.auth.response.AuthenticationResponse;

public interface AuthenticationService {

    AuthenticationResponse login(AuthenticationRequest request);

    void register(RegistrationRequest request);

    AuthenticationResponse refreshToken(RefreshRequest req);

    void smsUserVerification(SmsVerificationRequest req);

    void forgotPassword(ForgotPasswordRequest req);

    void resetPassword(ResetPasswordRequest req);
}