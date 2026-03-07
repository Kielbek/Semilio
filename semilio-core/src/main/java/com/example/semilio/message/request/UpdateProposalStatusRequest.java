package com.example.semilio.message.request;

import com.example.semilio.message.enums.ProposalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record UpdateProposalStatusRequest(
        @NotNull(message = "VALIDATION.PROPOSAL.STATUS.NOT_NULL")
        ProposalStatus newStatus
) {}