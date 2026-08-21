package com.example.backend.controller;

import com.example.backend.dto.ApiDocsResponse;
import com.example.backend.dto.EndpointDoc;
import com.example.backend.dto.EndpointGroupDoc;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * DocsController
 * --------------
 * A hand-written, plain-JSON API reference at GET /api/docs, separate from
 * the auto-generated Swagger UI (/swagger-ui.html) and OpenAPI spec
 * (/v3/api-docs). Useful for consumers that want a quick, human-readable
 * summary without loading the Swagger UI, or want to fetch the endpoint
 * list programmatically. Content here is static and must be kept in sync
 * with the controllers by hand.
 */
@RestController
@RequestMapping("/api/docs")
@Tag(name = "Docs", description = "Plain-JSON summary of every endpoint in this API.")
public class DocsController {

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "API reference", description = "Returns a static summary of every endpoint, grouped by resource, with auth requirements.")
    public ApiDocsResponse getDocs() {
        return new ApiDocsResponse(
                "Course Enrolment API",
                "v1",
                "REST API for the Course Enrolment System capstone project: user registration/login (JWT), "
                        + "course catalog management, student enrolments, and admin reporting.",
                "Send 'Authorization: Bearer <token>' on any endpoint whose auth is not 'public'. "
                        + "Obtain a token from POST /api/auth/login or POST /api/auth/register.",
                List.of(
                        new EndpointGroupDoc(
                                "Auth",
                                "/api/auth",
                                "Registration and login. Both endpoints are public and return a JWT.",
                                List.of(
                                        new EndpointDoc("POST", "/api/auth/register",
                                                "Create an account (always role STUDENT); returns a JWT", "public"),
                                        new EndpointDoc("POST", "/api/auth/login",
                                                "Authenticate with email/password; returns a JWT", "public")
                                )),
                        new EndpointGroupDoc(
                                "Courses",
                                "/api/courses",
                                "Course catalog. Reads need any logged-in user; writes need ADMIN.",
                                List.of(
                                        new EndpointDoc("GET", "/api/courses",
                                                "Search/paginate courses (keyword, category, level, active, page, size, sortBy, direction)",
                                                "authenticated"),
                                        new EndpointDoc("GET", "/api/courses/{id}",
                                                "Get a course by id", "authenticated"),
                                        new EndpointDoc("POST", "/api/courses",
                                                "Create a course", "ADMIN"),
                                        new EndpointDoc("PUT", "/api/courses/{id}",
                                                "Update a course (rejected if new capacity < current enrolled count)", "ADMIN"),
                                        new EndpointDoc("PATCH", "/api/courses/{id}/deactivate",
                                                "Soft-delete: blocks new enrolments, keeps history", "ADMIN"),
                                        new EndpointDoc("PATCH", "/api/courses/{id}/activate",
                                                "Reactivate a course", "ADMIN")
                                )),
                        new EndpointGroupDoc(
                                "Enrolments",
                                "/api/enrolments",
                                "Students enrol in courses; admins can view and cancel any enrolment.",
                                List.of(
                                        new EndpointDoc("POST", "/api/enrolments",
                                                "Enrol yourself in a course (fails if inactive, full, or already enrolled)", "STUDENT"),
                                        new EndpointDoc("GET", "/api/enrolments/my",
                                                "List your own enrolments", "authenticated"),
                                        new EndpointDoc("GET", "/api/enrolments",
                                                "List every enrolment", "ADMIN"),
                                        new EndpointDoc("DELETE", "/api/enrolments/{id}",
                                                "Cancel an enrolment (students: own only, 403 otherwise; admins: any)", "authenticated")
                                )),
                        new EndpointGroupDoc(
                                "Reports",
                                "/api/reports",
                                "Admin dashboard aggregate counts.",
                                List.of(
                                        new EndpointDoc("GET", "/api/reports/enrolments-by-course",
                                                "Enrolment counts grouped by course", "ADMIN"),
                                        new EndpointDoc("GET", "/api/reports/enrolments-by-category",
                                                "Enrolment counts grouped by course category", "ADMIN"),
                                        new EndpointDoc("GET", "/api/reports/monthly-enrolments",
                                                "Enrolment counts grouped by month", "ADMIN")
                                ))
                ));
    }
}
