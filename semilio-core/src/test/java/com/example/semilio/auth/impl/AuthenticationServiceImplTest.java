package com.example.semilio.auth.impl;

import com.example.semilio.auth.request.AuthenticationRequest;
import com.example.semilio.auth.request.RegistrationRequest;
import com.example.semilio.auth.response.AuthenticationResponse;
import com.example.semilio.auth.service.impl.AuthenticationServiceImpl;
import com.example.semilio.comon.NicknameGenerator;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.notification.email.EmailService;
import com.example.semilio.notification.sms.SmsService;
import com.example.semilio.notification.email.EmailTemplates;
import com.example.semilio.role.Role;
import com.example.semilio.role.RoleName;
import com.example.semilio.role.RoleRepository;
import com.example.semilio.security.JwtService;
import com.example.semilio.security.SecurityUser;
import com.example.semilio.token.PasswordResetTokenRepository;
import com.example.semilio.token.SmsTokenVerification;
import com.example.semilio.token.SmsTokenVerificationRepository;
import com.example.semilio.user.mapper.UserMapper;
import com.example.semilio.user.model.User;
import com.example.semilio.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private SmsTokenVerificationRepository tokenVerificationRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private UserMapper userMapper;
    @Mock private SmsService smsService;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private NicknameGenerator nicknameGenerator;

    @InjectMocks
    private AuthenticationServiceImpl authService;

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should return tokens when credentials are valid")
        void shouldReturnTokensWhenCredentialsAreValid() {
            AuthenticationRequest request = mock(AuthenticationRequest.class);
            Authentication auth = mock(Authentication.class);
            SecurityUser securityUser = mock(SecurityUser.class);

            given(request.email()).willReturn("test@example.com");
            given(request.password()).willReturn("password123");
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(auth);
            given(auth.getPrincipal()).willReturn(securityUser);
            given(securityUser.getUsername()).willReturn("test@example.com");
            given(jwtService.generateAccessToken("test@example.com")).willReturn("access-token");
            given(jwtService.generateRefreshToken("test@example.com")).willReturn("refresh-token");

            AuthenticationResponse result = authService.login(request);

            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            assertThat(result.tokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("Should throw exception when authentication manager fails")
        void shouldThrowExceptionWhenAuthenticationManagerFails() {
            AuthenticationRequest request = mock(AuthenticationRequest.class);

            given(request.email()).willReturn("test@example.com");
            given(request.password()).willReturn("wrong-password");
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            then(jwtService).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should successfully register a new user")
        void shouldRegisterUserSuccessfully() {
            RegistrationRequest request = mock(RegistrationRequest.class);
            Role userRole = new Role();
            userRole.setName(RoleName.USER);
            User mappedUser = new User();

            given(request.email()).willReturn("new@example.com");
            given(request.phoneNumber()).willReturn("123456789");
            given(request.password()).willReturn("Pass123!");
            given(request.confirmPassword()).willReturn("Pass123!");
            given(request.firstName()).willReturn("John");
            given(request.lastName()).willReturn("Doe");

            given(userRepository.existsByEmailIgnoreCase("new@example.com")).willReturn(false);
            given(userRepository.existsByPhoneNumber("123456789")).willReturn(false);
            given(roleRepository.findByName(RoleName.USER)).willReturn(Optional.of(userRole));
            given(userMapper.toUser(request)).willReturn(mappedUser);
            given(nicknameGenerator.generateNickname("John", "Doe")).willReturn("john_doe_1");
            given(userRepository.existsByNickName("john_doe_1")).willReturn(false);
            given(tokenVerificationRepository.existsByCode(anyString())).willReturn(false);

            authService.register(request);

            assertThat(mappedUser.getNickName()).isEqualTo("john_doe_1");
            assertThat(mappedUser.getRoles()).containsExactly(userRole);
            then(userRepository).should(times(1)).save(mappedUser);
            then(tokenVerificationRepository).should(times(1)).save(any(SmsTokenVerification.class));
            then(roleRepository).should(times(1)).save(userRole);
        }

        @Test
        @DisplayName("Should retry generating nickname if first one exists")
        void shouldGenerateUniqueNicknameRetryWhenExists() {
            RegistrationRequest request = mock(RegistrationRequest.class);
            Role userRole = new Role();
            User mappedUser = new User();

            given(request.password()).willReturn("Pass123!");
            given(request.confirmPassword()).willReturn("Pass123!");
            given(roleRepository.findByName(RoleName.USER)).willReturn(Optional.of(userRole));
            given(userMapper.toUser(request)).willReturn(mappedUser);

            given(nicknameGenerator.generateNickname(any(), any()))
                    .willReturn("john_doe_1")
                    .willReturn("john_doe_2");

            given(userRepository.existsByNickName("john_doe_1")).willReturn(true);
            given(userRepository.existsByNickName("john_doe_2")).willReturn(false);
            given(tokenVerificationRepository.existsByCode(anyString())).willReturn(false);

            authService.register(request);

            assertThat(mappedUser.getNickName()).isEqualTo("john_doe_2");
            then(nicknameGenerator).should(times(2)).generateNickname(any(), any());
            then(userRepository).should(times(2)).existsByNickName(anyString());
        }

        @Test
        @DisplayName("Should throw BusinessException when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            RegistrationRequest request = mock(RegistrationRequest.class);
            given(request.email()).willReturn("taken@example.com");
            given(userRepository.existsByEmailIgnoreCase("taken@example.com")).willReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_ALREADY_EXISTS);

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessException when phone number already exists")
        void shouldThrowExceptionWhenPhoneAlreadyExists() {
            RegistrationRequest request = mock(RegistrationRequest.class);
            given(request.phoneNumber()).willReturn("111222333");
            given(userRepository.existsByPhoneNumber("111222333")).willReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PHONE_ALREADY_EXISTS);

            then(userRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("Should throw BusinessException when passwords do not match")
        void shouldThrowExceptionWhenPasswordsDoNotMatch() {
            RegistrationRequest request = mock(RegistrationRequest.class);
            given(request.password()).willReturn("Pass123!");
            given(request.confirmPassword()).willReturn("DifferentPass!");

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_MISMATCH);

            then(userRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should return new access token when refresh token is valid")
        void shouldReturnNewAccessTokenWhenRefreshTokenIsValid() {
            com.example.semilio.auth.request.RefreshRequest request = mock(com.example.semilio.auth.request.RefreshRequest.class);
            given(request.refreshToken()).willReturn("valid-refresh-token");
            given(jwtService.refreshAccessToken("valid-refresh-token")).willReturn("new-access-token");

            AuthenticationResponse result = authService.refreshToken(request);

            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isEqualTo("valid-refresh-token");
            assertThat(result.tokenType()).isEqualTo("Bearer");
        }
    }

    @Nested
    @DisplayName("SMS Verification Tests")
    class SmsVerificationTests {

        @Test
        @DisplayName("Should verify SMS successfully when code is valid and not expired")
        void shouldVerifySmsSuccessfully() {
            com.example.semilio.auth.request.SmsVerificationRequest request = mock(com.example.semilio.auth.request.SmsVerificationRequest.class);
            SmsTokenVerification token = new SmsTokenVerification();
            token.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(5));
            User user = new User();
            user.setPhoneVerified(false);
            token.setUser(user);

            given(request.code()).willReturn("123456");
            given(tokenVerificationRepository.findByCode("123456")).willReturn(Optional.of(token));

            authService.smsUserVerification(request);

            assertThat(user.isPhoneVerified()).isTrue();
            assertThat(token.getCode()).isNull();
            assertThat(token.getValidatedAt()).isNotNull();
            then(userRepository).should(times(1)).save(user);
            then(tokenVerificationRepository).should(times(1)).save(token);
        }

        @Test
        @DisplayName("Should throw exception when verification code is invalid")
        void shouldThrowExceptionWhenTokenIsInvalid() {
            com.example.semilio.auth.request.SmsVerificationRequest request = mock(com.example.semilio.auth.request.SmsVerificationRequest.class);
            given(request.code()).willReturn("000000");
            given(tokenVerificationRepository.findByCode("000000")).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.smsUserVerification(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_VERIFICATION_CODE);

            then(userRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw exception when verification code is expired")
        void shouldThrowExceptionWhenTokenIsExpired() {
            com.example.semilio.auth.request.SmsVerificationRequest request = mock(com.example.semilio.auth.request.SmsVerificationRequest.class);
            SmsTokenVerification token = new SmsTokenVerification();
            token.setExpiresAt(java.time.LocalDateTime.now().minusMinutes(5));

            given(request.code()).willReturn("123456");
            given(tokenVerificationRepository.findByCode("123456")).willReturn(Optional.of(token));

            assertThatThrownBy(() -> authService.smsUserVerification(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPIRED_VERIFICATION_CODE);

            then(userRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordResetTests {

        @Test
        @DisplayName("Should send forgot password email successfully")
        void shouldSendForgotPasswordEmailSuccessfully() {
            com.example.semilio.auth.request.ForgotPasswordRequest request = mock(com.example.semilio.auth.request.ForgotPasswordRequest.class);
            User user = new User();
            user.setEmail("user@example.com");
            user.setFirstName("Alice");

            given(request.email()).willReturn("user@example.com");
            given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(user));

            authService.forgotPassword(request);

            then(passwordResetTokenRepository).should(times(1)).save(any(com.example.semilio.token.PasswordResetToken.class));
            then(emailService).should(times(1)).sendEmail(
                    org.mockito.ArgumentMatchers.eq("user@example.com"),
                    any(EmailTemplates.class),
                    any(java.util.Map.class)
            );
        }

        @Test
        @DisplayName("Should throw exception when user not found during forgot password")
        void shouldThrowExceptionWhenUserNotFoundDuringForgotPassword() {
            com.example.semilio.auth.request.ForgotPasswordRequest request = mock(com.example.semilio.auth.request.ForgotPasswordRequest.class);
            given(request.email()).willReturn("unknown@example.com");
            given(userRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> authService.forgotPassword(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);

            then(passwordResetTokenRepository).shouldHaveNoInteractions();
            then(emailService).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should reset password successfully when token is valid")
        void shouldResetPasswordSuccessfully() {
            com.example.semilio.auth.request.ResetPasswordRequest request = mock(com.example.semilio.auth.request.ResetPasswordRequest.class);
            com.example.semilio.token.PasswordResetToken token = new com.example.semilio.token.PasswordResetToken();
            token.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));
            User user = new User();
            token.setUser(user);

            given(request.token()).willReturn("valid-hash");
            given(request.password()).willReturn("NewPass123!");
            given(request.confirmPassword()).willReturn("NewPass123!");
            given(passwordResetTokenRepository.findByTokenHash("valid-hash")).willReturn(Optional.of(token));
            given(passwordEncoder.encode("NewPass123!")).willReturn("encoded-pass");

            authService.resetPassword(request);

            assertThat(user.getPassword()).isEqualTo("encoded-pass");
            assertThat(token.getValidatedAt()).isNotNull();
            then(passwordResetTokenRepository).should(times(1)).save(token);
            then(userRepository).should(times(1)).save(user);
        }

        @Test
        @DisplayName("Should throw exception when reset token is expired")
        void shouldThrowExceptionWhenResetTokenIsExpired() {
            com.example.semilio.auth.request.ResetPasswordRequest request = mock(com.example.semilio.auth.request.ResetPasswordRequest.class);
            com.example.semilio.token.PasswordResetToken token = new com.example.semilio.token.PasswordResetToken();
            token.setExpiresAt(java.time.LocalDateTime.now().minusMinutes(10));

            given(request.token()).willReturn("expired-hash");
            given(passwordResetTokenRepository.findByTokenHash("expired-hash")).willReturn(Optional.of(token));

            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPIRED_RESET_TOKEN);

            then(userRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw exception when passwords mismatch during reset")
        void shouldThrowExceptionWhenPasswordsMismatchDuringReset() {
            com.example.semilio.auth.request.ResetPasswordRequest request = mock(com.example.semilio.auth.request.ResetPasswordRequest.class);
            com.example.semilio.token.PasswordResetToken token = new com.example.semilio.token.PasswordResetToken();
            token.setExpiresAt(java.time.LocalDateTime.now().plusMinutes(10));

            given(request.token()).willReturn("valid-hash");
            given(request.password()).willReturn("NewPass123!");
            given(request.confirmPassword()).willReturn("DifferentPass!");
            given(passwordResetTokenRepository.findByTokenHash("valid-hash")).willReturn(Optional.of(token));

            assertThatThrownBy(() -> authService.resetPassword(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_MISMATCH);

            then(passwordEncoder).shouldHaveNoInteractions();
            then(userRepository).shouldHaveNoInteractions();
        }
    }
}