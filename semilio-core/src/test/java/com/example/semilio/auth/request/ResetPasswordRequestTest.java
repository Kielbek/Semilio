package com.example.semilio.auth.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResetPasswordRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Should pass when all fields are valid")
    void shouldPassWhenValid() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("some-uuid-token")
                .password("Password123")
                .confirmPassword("Password123")
                .build();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("Should fail when token is blank")
    void shouldFailWhenTokenIsBlank() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("  ")
                .password("Password123")
                .confirmPassword("Password123")
                .build();

        var violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("VALIDATION.AUTHENTICATION.RESET_PASSWORD.TOKEN.NOT_BLANK"));
    }

    @Test
    @DisplayName("Should fail when password does not meet requirements (weak)")
    void shouldFailWhenPasswordIsWeak() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("uuid")
                .password("password")
                .confirmPassword("password")
                .build();

        var violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("VALIDATION.REGISTRATION.PASSWORD.WEAK"));
    }

    @Test
    @DisplayName("Should fail when password is too short")
    void shouldFailWhenPasswordIsTooShort() {
        ResetPasswordRequest request = ResetPasswordRequest.builder()
                .token("uuid")
                .password("Ab1")
                .confirmPassword("Ab1")
                .build();

        var violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("VALIDATION.REGISTRATION.PASSWORD.SIZE"));
    }
}