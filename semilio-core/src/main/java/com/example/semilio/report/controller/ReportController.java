package com.example.semilio.report.controller;

import com.example.semilio.report.service.ReportService;
import com.example.semilio.report.request.CreateReportRequest;
import com.example.semilio.report.response.ReportCategoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/categories")
    public List<ReportCategoryResponse> getCategories(Locale locale) {
        return reportService.getCategories(locale);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void submitReport(
            @Valid @RequestBody CreateReportRequest request,
            Authentication principal
    ) {
        reportService.submitReport(request, principal);
    }
}