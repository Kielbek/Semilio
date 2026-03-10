package com.example.semilio.report.service.impl;

import com.example.semilio.config.ReportDataConfig;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.notification.email.EmailService;
import com.example.semilio.notification.email.EmailTemplates;
import com.example.semilio.report.enums.ReportStatus;
import com.example.semilio.report.model.Report;
import com.example.semilio.report.repository.ReportRepository;
import com.example.semilio.report.request.CreateReportRequest;
import com.example.semilio.report.response.ReportCategoryResponse;
import com.example.semilio.report.service.ReportService;
import com.example.semilio.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final MessageSource messageSource;
    private final EmailService emailService;
    private final SecurityService securityService;
    private final ReportDataConfig.PolishCategories polish;

    public List<ReportCategoryResponse> getCategories(Locale locale) {
        log.debug("Fetching report categories for locale: {}", locale);
        return polish.getCategories();
    }

    @Override
    @Transactional
    public void submitReport(CreateReportRequest request, Authentication principal) {
        String reporterEmail = securityService.getCurrentUser(principal).getEmail();
        log.info("Starting report submission for target: {} by reporter: {}", request.targetId(), reporterEmail);

        boolean alreadyReported = reportRepository.existsByTargetIdAndReporterEmail(
                request.targetId(),
                reporterEmail
        );

        if (alreadyReported) {
            log.warn("User {} already reported target {}", reporterEmail, request.targetId());
            throw new BusinessException(ErrorCode.REPORT_ALREADY_EXISTS);
        }

        Report report = Report.builder()
                .targetId(request.targetId())
                .reason(request.reason())
                .subReasonId(request.subReasonId())
                .description(request.description())
                .reporterEmail(reporterEmail)
                .status(ReportStatus.NEW)
                .build();

        try {

            Report savedReport = reportRepository.save(report);

            log.info("Report successfully saved with ID: {}", savedReport.getId().toString());

            Map<String, Object> emailVariables = Map.of(
                    "reportId", savedReport.getId().toString().substring(0, 8),
                    "category", savedReport.getReason().name(),
                    "reportDate", new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date())
            );

            log.debug("Attempting to send confirmation email to {}", savedReport.getReporterEmail());
            emailService.sendEmail(
                    savedReport.getReporterEmail(),
                    EmailTemplates.REPORT_CONFIRMATION,
                    emailVariables
            );
            log.info("Confirmation email sent successfully to {}", savedReport.getReporterEmail());

        } catch (Exception e) {
            log.error("Failed to process email notification for report: {}", report.getTargetId().toString(), e);
        }
    }
}