package com.example.backend.controller;

import com.example.backend.dto.CreateEnrolmentRequest;
import com.example.backend.dto.EnrolmentResponse;
import com.example.backend.security.AuthenticatedUser;
import com.example.backend.service.EnrolmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * EnrolmentController
 * --------------------
 * Role split (enforced in SecurityConfig):
 * - POST /api/enrolments        STUDENT enrols themselves in a course.
 * - GET  /api/enrolments/my     any logged-in user views their own.
 * - GET  /api/enrolments        ADMIN views every enrolment.
 * - DELETE /api/enrolments/{id} cancel - ownership is checked in the
 *   service, since a student may only cancel their own but an admin may
 *   cancel anyone's.
 *
 * @AuthenticationPrincipal AuthenticatedUser pulls the logged-in user
 * straight off the token that JwtAuthenticationFilter already decoded -
 * no separate lookup needed to know "who is asking".
 */
@RestController
@RequestMapping("/api/enrolments")
@Tag(name = "Enrolments", description = "Students enrol in courses; admins can view and cancel any enrolment.")
public class EnrolmentController {

    private final EnrolmentService enrolmentService;

    public EnrolmentController(EnrolmentService enrolmentService) {
        this.enrolmentService = enrolmentService;
    }

    @PostMapping
    @Operation(summary = "Enrol in a course", description = "STUDENT only. Fails if the course is inactive, full, or already actively enrolled.")
    public ResponseEntity<EnrolmentResponse> enrol(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateEnrolmentRequest request) {

        EnrolmentResponse created = enrolmentService.enrol(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/my")
    @Operation(summary = "List my enrolments", description = "Any logged-in user; returns only the caller's own enrolments.")
    public List<EnrolmentResponse> getMyEnrolments(@AuthenticationPrincipal AuthenticatedUser currentUser) {
        return enrolmentService.getMyEnrolments(currentUser.id());
    }

    @GetMapping
    @Operation(summary = "List every enrolment", description = "ADMIN only.")
    public List<EnrolmentResponse> getAllEnrolments() {
        return enrolmentService.getAllEnrolments();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel an enrolment", description = "Any logged-in user, but a STUDENT may only cancel their own (403 otherwise); ADMIN can cancel any.")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String id) {

        enrolmentService.cancel(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}
