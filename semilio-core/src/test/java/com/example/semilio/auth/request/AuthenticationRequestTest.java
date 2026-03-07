package com.example.semilio.auth.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Should pass when all fields are correct")
    void shouldPassWhenAllFieldsAreValid() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("Pass1234")
                .build();

        var violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail when email is invalid")
    void shouldFailWhenEmailIsInvalid() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("invalid-email")
                .password("Pass1234")
                .build();

        var violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getMessage().equals("VALIDATION.AUTHENTICATION.EMAIL.FORMAT"));
    }

    @Test
    @DisplayName("Should fail when password is too weak")
    void shouldFailWhenPasswordIsWeak() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("short")
                .build();

        var violations = validator.validate(request);

        assertThat(violations).anyMatch(v ->
                v.getMessage().equals("VALIDATION.REGISTRATION.PASSWORD.WEAK"));
    }

    @Test
    @DisplayName("Should fail when fields are blank")
    void shouldFailWhenFieldsAreBlank() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("")
                .password("")
                .build();

        var violations = validator.validate(request);

        assertThat(violations).hasSize(3);
    }
}