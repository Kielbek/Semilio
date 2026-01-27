package com.example.semilio.auth.impl;

import com.example.semilio.auth.request.*;
import com.example.semilio.auth.response.AuthenticationResponse;
import com.example.semilio.comon.NicknameGenerator;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.notification.EmailService;
import com.example.semilio.notification.EmailTemplates;
import com.example.semilio.notification.SmsService;
import com.example.semilio.role.Role;
import com.example.semilio.role.RoleName;
import com.example.semilio.role.RoleRepository;
import com.example.semilio.security.JwtService;
import com.example.semilio.token.PasswordResetToken;
import com.example.semilio.token.PasswordResetTokenRepository;
import com.example.semilio.token.SmsTokenVerification;
import com.example.semilio.token.SmsTokenVerificationRepository;
import com.example.semilio.user.User;
import com.example.semilio.user.UserMapper;
import com.example.semilio.user.UserRepository;
import com.example.semilio.auth.AuthenticationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static com.example.semilio.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SmsTokenVerificationRepository tokenVerificationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserMapper userMapper;
    private final SmsService smsService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final NicknameGenerator nicknameGenerator;

    @Override
    public AuthenticationResponse login(final AuthenticationRequest request) {
        final Authentication auth = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        final User user = (User) auth.getPrincipal();
        final String token = this.jwtService.generateAccessToken(user.getUsername());
        final String refreshToken = this.jwtService.generateRefreshToken(user.getUsername());
        final String tokenType = "Bearer";
        return AuthenticationResponse.builder()
                .accessToken(token)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .build();
    }

    @Override
    @Transactional
    public void register(final RegistrationRequest request) {
        checkUserEmail(request.getEmail());
        checkUserPhoneNumber(request.getPhoneNumber());
        checkPasswords(request.getPassword(), request.getConfirmPassword());

        final Role userRole = this.roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new EntityNotFoundException("Role user does not exist"));
        final List<Role> roles = new ArrayList<>();
        roles.add(userRole);

        final User user = this.userMapper.toUser(request);

        String uniqueNickname = generateUniqueNickname(request.getFirstName(), request.getLastName());
        user.setNickName(uniqueNickname);

        user.setRoles(roles);
        log.debug("Saving user {}", user);
        this.userRepository.save(user);

        final List<User> users = new ArrayList<>();
        users.add(user);
        userRole.setUsers(users);

        generateAndSendActivationToken(user);

        this.roleRepository.save(userRole);
    }

    @Override
    public AuthenticationResponse refreshToken(final RefreshRequest req) {
        final String newAccessToken = this.jwtService.refreshAccessToken(req.getRefreshToken());
        final String tokenType = "Bearer";
        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(req.getRefreshToken())
                .tokenType(tokenType)
                .build();
    }

    @Override
    public void smsUserVerification(SmsVerificationRequest req) {
        SmsTokenVerification token = this.tokenVerificationRepository.findByCode(req.getCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_VERIFICATION_CODE));

        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new BusinessException(ErrorCode.EXPIRED_VERIFICATION_CODE);
        }

        User user = token.getUser();

        user.setPhoneVerified(true);

        token.setValidatedAt(LocalDateTime.now());
        token.setCode(null);

        this.userRepository.save(user);
        this.tokenVerificationRepository.save(token);

        log.info("User {} with phone {} successfully verified their phone via SMS",
                user.getId(), user.getPhoneNumber());
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest req) {
        User user = this.userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String rawToken = UUID.randomUUID().toString();

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .tokenHash(rawToken)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        this.passwordResetTokenRepository.save(passwordResetToken);

        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", user.getFirstName());
        variables.put("resetLink", "http://localhost:4200/reset-password?token=" + rawToken);

        this.emailService.sendEmail(user.getEmail(), EmailTemplates.RESET_PASSWORD, variables);
    }

    @Override
    public void resetPassword(ResetPasswordRequest req) {
        PasswordResetToken passwordResetToken = this.passwordResetTokenRepository.findByTokenHash(req.getToken())
                .orElseThrow(() -> new BusinessException(INVALID_RESET_TOKEN));

        if (LocalDateTime.now().isAfter(passwordResetToken.getExpiresAt())) {
            throw new BusinessException(ErrorCode.EXPIRED_RESET_TOKEN);
        }

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        passwordResetToken.setValidatedAt(LocalDateTime.now());

        this.passwordResetTokenRepository.save(passwordResetToken);
        this.userRepository.save(user);
    }

    private void checkUserEmail(final String email) {
        final boolean emailExists = this.userRepository.existsByEmailIgnoreCase(email);
        if (emailExists) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);
        }
    }

    private void checkPasswords(final String password,
                                final String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new BusinessException(PASSWORD_MISMATCH);
        }
    }

    private void checkUserPhoneNumber(final String phoneNumber) {
        final boolean phoneNumberExists = this.userRepository.existsByPhoneNumber(phoneNumber);
        if (phoneNumberExists) {
            throw new BusinessException(PHONE_ALREADY_EXISTS);
        }
    }

    private void generateAndSendActivationToken(User user) {

        int generatedToken = generateVerificationCode();

        if (tokenVerificationRepository.existsByCode(generatedToken)) {
            generateAndSendActivationToken(user);
            return;
        }

        SmsTokenVerification token = SmsTokenVerification.builder()
                .code(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .user(user)
                .build();

        this.tokenVerificationRepository.save(token);

        String message = String.format("Twój kod weryfikacyjny to: %06d. Kod ważny 15 minut.", generatedToken);
//        this.smsService.sendSms(user.getPhoneNumber(), message);

        log.info("Generated and sent verification SMS for user {} with phone number {}", user.getId(), user.getPhoneNumber());
    }

    private int generateVerificationCode() {
        Random random = new Random();

        return 100000 + random.nextInt(900000);
    }

    private String generateUniqueNickname(String firstName, String lastName) {
        String nickname;
        boolean exists;
        do {
            nickname = nicknameGenerator.generateNickname(firstName, lastName);
            exists = userRepository.existsByNickName(nickname);
        } while (exists);

        return nickname;
    }
}