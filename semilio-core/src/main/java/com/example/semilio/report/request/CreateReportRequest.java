package com.example.semilio.report.request;

import com.example.semilio.report.enums.ReportReason;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateReportRequest(

        @NotNull(message = "Target identifier cannot be blank")
        UUID targetId,

        @NotNull(message = "Main report reason is required")
        ReportReason reason,

        String subReasonId,

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
        String description,

        @AssertTrue(message = "A good faith declaration is required (DSA requirement)")
        boolean declaration

) {}