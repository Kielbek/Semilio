package com.example.semilio.product.request;

import com.example.semilio.product.enums.Condition;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("Should pass validation when all fields are valid")
    void shouldPassValidationForValidRequest() {
        ProductRequest request = validRequestBuilder().build();
        Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Nested
    @DisplayName("Title Validation")
    class TitleValidation {

        @Test
        @DisplayName("Should fail when title is blank")
        void shouldFailWhenTitleIsBlank() {
            ProductRequest request = validRequestBuilder().title("   ").build();
            Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

            assertThat(violations).extracting(ConstraintViolation::getMessage)
                    .contains("VALIDATION.PRODUCT.TITLE.BLANK");
        }

        @Test
        @DisplayName("Should fail when title is too short (< 5 chars)")
        void shouldFailWhenTitleIsTooShort() {
            ProductRequest request = validRequestBuilder().title("abcd").build();
            Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

            assertThat(violations).extracting(ConstraintViolation::getMessage)
                    .contains("VALIDATION.PRODUCT.TITLE.SIZE");
        }
    }

    @Nested
    @DisplayName("Description Validation")
    class DescriptionValidation {

        @Test
        @DisplayName("Should fail when description is blank")
        void shouldFailWhenDescriptionIsBlank() {
            ProductRequest request = validRequestBuilder().description("").build();
            Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

            assertThat(violations).extracting(ConstraintViolation::getMessage)
                    .contains("VALIDATION.PRODUCT.DESCRIPTION.BLANK");
        }

        @Test
        @DisplayName("Should fail when description is too short")
        void shouldFailWhenDescriptionIsTooShort() {
            ProductRequest request = validRequestBuilder().description("Too short").build();
            Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

            assertThat(violations).extracting(ConstraintViolation::getMessage)
                    .contains("VALIDATION.PRODUCT.DESCRIPTION.SIZE");
        }
    }

    @Nested
    @DisplayName("Amount Validation")
    class AmountValidation {

        @Test
        @DisplayName("Should fail when amount is null")
        void shouldFailWhenAmountIsNull() {
            ProductRequest request = validRequestBuilder().amount(null).build();
            Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

            assertThat(violations).extracting(ConstraintViolation::getMessage)
                    .contains("VALIDATION.PRODUCT.PRICE.NOT_NULL");
        }

        @Test
        @DisplayName("Should fail when amount is zero or negative")
        void shouldFailWhenAmountIsInvalid() {
            ProductRequest request = validRequestBuilder().amount(new BigDecimal("0.00")).build();
            Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

            assertThat(violations).extracting(ConstraintViolation::getMessage)
                    .contains("VALIDATION.PRODUCT.PRICE.MIN");
        }
    }

    @Nested
    @DisplayName("Foreign Keys and Enums Validation")
    class IdentifiersValidation {

        @Test
        @DisplayName("Should fail when required IDs are null")
        void shouldFailWhenIdsAreNull() {
            ProductRequest request = validRequestBuilder()
                    .sizeId(null)
                    .brandId(null)
                    .colorId(null)
                    .categoryId(null)
                    .condition(null)
                    .build();

            Set<ConstraintViolation<ProductRequest>> violations = validator.validate(request);

            assertThat(violations).extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "VALIDATION.PRODUCT.SIZE.NOT_NULL",
                            "VALIDATION.PRODUCT.BRAND.NOT_NULL",
                            "VALIDATION.PRODUCT.COLOR.NOT_NULL",
                            "VALIDATION.PRODUCT.CATEGORY.NOT_NULL",
                            "VALIDATION.PRODUCT.CONDITION.NOT_NULL"
                    );
        }
    }

    private ProductRequest.ProductRequestBuilder validRequestBuilder() {
        return ProductRequest.builder()
                .title("Valid Product Title")
                .description("This description is perfectly valid because it has more than twenty characters.")
                .amount(new BigDecimal("99.99"))
                .sizeId(1L)
                .brandId(2L)
                .colorId(3L)
                .categoryId(4L)
                .condition(Condition.NEW_WITH_TAGS);
    }
}