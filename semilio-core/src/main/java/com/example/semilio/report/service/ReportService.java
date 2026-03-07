package com.example.semilio.report.service;

import com.example.semilio.report.request.CreateReportRequest;
import com.example.semilio.report.response.ReportCategoryResponse;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Locale;

public interface ReportService {

    List<ReportCategoryResponse> getCategories(Locale locale);

    void submitReport(CreateReportRequest request, Authentication principal);

}