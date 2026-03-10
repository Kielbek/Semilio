package com.example.semilio.report.repository;

import com.example.semilio.report.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRepository  extends JpaRepository<Report, UUID> {

    boolean existsByTargetIdAndReporterEmail(UUID targetId, String reporterEmail);

}
