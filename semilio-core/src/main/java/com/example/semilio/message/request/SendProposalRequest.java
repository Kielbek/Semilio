package com.example.semilio.message.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record SendProposalRequest(
        Long chatId,

        UUID productId,

        @NotNull(message = "VALIDATION.PROPOSAL.AMOUNT.NOT_NULL")
        @Positive(message = "VALIDATION.PROPOSAL.AMOUNT.POSITIVE")
        BigDecimal amount
) {}