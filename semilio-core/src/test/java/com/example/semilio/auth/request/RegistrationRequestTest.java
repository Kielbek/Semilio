package com.example.semilio.auth.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private RegistrationRequest.RegistrationRequestBuilder validBuilder() {
        return RegistrationRequest.builder()
                .firstName("Jan")
                .lastName("Kowalski")
                .email("jan.kowalski@example.com")
                .phoneNumber("+48123456789")
                .password("Password123")
                .confirmPassword("Password123");
    }

    @Test
    @DisplayName("Should pass when all fields are valid")
    void shouldPassValidationForValidRequest() {
        RegistrationRequest request = validBuilder().build();
        assertThat(validator.validate(request)).isEmpty();
    }

    @Nested
    @DisplayName("Name Validation")
    class NameValidation {

        @Test
        @DisplayName("Should fail when names have invalid characters")
        void shouldFailWhenNameHasNumbers() {
            RegistrationRequest request = validBuilder()
                    .firstName("Jan123")
                    .lastName("Kowalski!")
                    .build();

            var violations = validator.validate(request);

            assertThat(violations).anyMatch(v -> v.getMessage().equals("VALIDATION.REGISTRATION.FIRST_NAME.PATTERN"));
            assertThat(violations).anyMatch(v -> v.getMessage().equals("VALIDATION.REGISTRATION.LAST_NAME.PATTERN"));
        }
    }

    @Nested
    @DisplayName("Email Validation")
    class EmailValidation {

        @Test
        @DisplayName("Should fail when email format is invalid")
        void shouldFailWhenEmailIsInvalid() {
            RegistrationRequest request = validBuilder().email("invalid-email").build();
            var violations = validator.validate(request);
            assertThat(violations).anyMatch(v -> v.getMessage().equals("VALIDATION.REGISTRATION.EMAIL.FORMAT"));
        }
    }

    @Nested
    @DisplayName("Phone Validation")
    class PhoneValidation {

        @Test
        @DisplayName("Should fail when phone format is invalid")
        void shouldFailWhenPhoneIsInvalid() {
            // Litery w numerze telefonu
            RegistrationRequest request = validBuilder().phoneNumber("123abc456").build();
            var violations = validator.validate(request);
            assertThat(violations).anyMatch(v -> v.getMessage().equals("VALIDATION.REGISTRATION.PHONE.FORMAT"));
        }
    }

    @Nested
    @DisplayName("Password Validation")
    class PasswordValidation {

        @Test
        @DisplayName("Should fail when password is weak")
        void shouldFailWhenPasswordIsWeak() {
            // Brakuje cyfry i wielkiej litery
            RegistrationRequest request = validBuilder().password("password").build();
            var violations = validator.validate(request);
            assertThat(violations).anyMatch(v -> v.getMessage().equals("VALIDATION.REGISTRATION.PASSWORD.WEAK"));
        }

        @Test
        @DisplayName("Should fail when password is too short")
        void shouldFailWhenPasswordIsTooShort() {
            RegistrationRequest request = validBuilder().password("Short1").build();
            var violations = validator.validate(request);
            assertThat(violations).anyMatch(v -> v.getMessage().equals("VALIDATION.REGISTRATION.PASSWORD.SIZE"));
        }
    }
}