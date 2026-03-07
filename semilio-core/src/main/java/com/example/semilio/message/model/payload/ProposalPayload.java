package com.example.semilio.message.model.payload;

import com.example.semilio.message.enums.ProposalStatus;

import java.math.BigDecimal;

public record ProposalPayload(
        BigDecimal amount,
        String currency,
        BigDecimal originalPrice,
        ProposalStatus status
) implements MessagePayload {}