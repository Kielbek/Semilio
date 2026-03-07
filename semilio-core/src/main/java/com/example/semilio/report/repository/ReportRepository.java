package com.example.semilio.report.repository;

import com.example.semilio.report.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository  extends JpaRepository<Report, String> {
    boolean existsByTargetIdAndReporterEmail(String targetId, String reporterEmail);
}
