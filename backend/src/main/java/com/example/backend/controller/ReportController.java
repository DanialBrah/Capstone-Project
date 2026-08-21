package com.example.backend.controller;

import com.example.backend.dto.ReportCountResponse;
import com.example.backend.service.EnrolmentReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// ADMIN-only, enforced in SecurityConfig - this is the admin dashboard's
// "simple report" requirement.
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Admin dashboard aggregate counts. ADMIN only.")
public class ReportController {

    private final EnrolmentReportService enrolmentReportService;

    public ReportController(EnrolmentReportService enrolmentReportService) {
        this.enrolmentReportService = enrolmentReportService;
    }

    @GetMapping("/enrolments-by-course")
    @Operation(summary = "Enrolment counts by course")
    public List<ReportCountResponse> getEnrolmentsByCourse() {
        return enrolmentReportService.countEnrolmentsByCourse();
    }

    @GetMapping("/enrolments-by-category")
    @Operation(summary = "Enrolment counts by course category")
    public List<ReportCountResponse> getEnrolmentsByCategory() {
        return enrolmentReportService.countEnrolmentsByCategory();
    }

    @GetMapping("/monthly-enrolments")
    @Operation(summary = "Enrolment counts by month")
    public List<ReportCountResponse> getMonthlyEnrolments() {
        return enrolmentReportService.countEnrolmentsByMonth();
    }
}
