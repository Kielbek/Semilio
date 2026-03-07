package com.example.semilio.auth.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SmsVerificationRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Should pass when code is valid 6-digit string")
    void shouldPassWhenCodeIsValid() {
        SmsVerificationRequest request = SmsVerificationRequest.builder()
                .code("001234")
                .build();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("Should fail when code is blank")
    void shouldFailWhenCodeIsBlank() {
        SmsVerificationRequest request = SmsVerificationRequest.builder()
                .code("   ")
                .build();

        var violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("VALIDATION.AUTHENTICATION.SMS.CODE_NOT_BLANK"));
    }

    @Test
    @DisplayName("Should fail when code length is not 6")
    void shouldFailWhenCodeLengthIsInvalid() {
        SmsVerificationRequest request = SmsVerificationRequest.builder()
                .code("12345")
                .build();

        var violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getMessage().equals("VALIDATION.AUTHENTICATION.SMS.CODE_INVALID_FORMAT"));
    }
}