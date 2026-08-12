package com.example.backend.controller;

import com.example.backend.dto.ReportCountResponse;
import com.example.backend.service.EnrolmentReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// ADMIN-only, enforced in SecurityConfig - this is the admin dashboard's
// "simple report" requirement.
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final EnrolmentReportService enrolmentReportService;

    public ReportController(EnrolmentReportService enrolmentReportService) {
        this.enrolmentReportService = enrolmentReportService;
    }

    @GetMapping("/enrolments-by-course")
    public List<ReportCountResponse> getEnrolmentsByCourse() {
        return enrolmentReportService.countEnrolmentsByCourse();
    }

    @GetMapping("/enrolments-by-category")
    public List<ReportCountResponse> getEnrolmentsByCategory() {
        return enrolmentReportService.countEnrolmentsByCategory();
    }

    @GetMapping("/monthly-enrolments")
    public List<ReportCountResponse> getMonthlyEnrolments() {
        return enrolmentReportService.countEnrolmentsByMonth();
    }
}
