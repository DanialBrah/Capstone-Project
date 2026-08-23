package com.example.backend.service;

import com.example.backend.dto.CreateEnrolmentRequest;
import com.example.backend.dto.EnrolmentResponse;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.exception.InvalidRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Course;
import com.example.backend.model.Enrolment;
import com.example.backend.repository.EnrolmentRepository;
import com.example.backend.security.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * EnrolmentService
 * ----------------
 * The "action/transaction" side of the domain. Every business rule from
 * the project brief lives here: no double-enrolling, no full courses, no
 * enrolling in inactive courses, and cancelling returns the seat.
 */
@Service
public class EnrolmentService {

    private static final Logger logger = LoggerFactory.getLogger(EnrolmentService.class);

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_CANCELLED = "CANCELLED";

    private final EnrolmentRepository enrolmentRepository;
    private final CourseService courseService;

    public EnrolmentService(EnrolmentRepository enrolmentRepository, CourseService courseService) {
        this.enrolmentRepository = enrolmentRepository;
        this.courseService = courseService;
    }

    public EnrolmentResponse enrol(AuthenticatedUser student, CreateEnrolmentRequest request) {
        Course course = courseService.findCourseOrThrow(request.getCourseId());

        if (!course.isActive()) {
            throw new InvalidRequestException("Course is not active: " + course.getTitle());
        }

        if (course.getEnrolledCount() >= course.getCapacity()) {
            throw new InvalidRequestException("Course is full: " + course.getTitle());
        }

        enrolmentRepository.findByStudentIdAndCourseIdAndStatus(student.id(), course.getId(), STATUS_ACTIVE)
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Already enrolled in this course: " + course.getTitle());
                });

        Enrolment enrolment = new Enrolment(
                student.id(),
                student.name(),
                student.email(),
                course.getId(),
                course.getTitle(),
                course.getCategory(),
                course.getLevel(),
                STATUS_ACTIVE
        );
        Enrolment savedEnrolment = enrolmentRepository.save(enrolment);

        course.setEnrolledCount(course.getEnrolledCount() + 1);
        courseService.save(course);

        logger.info("Student id={} enrolled in course id={}", student.id(), course.getId());

        return toResponse(savedEnrolment);
    }

    public List<EnrolmentResponse> getMyEnrolments(String studentId) {
        return enrolmentRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EnrolmentResponse> getAllEnrolments() {
        return enrolmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public void cancel(AuthenticatedUser currentUser, String enrolmentId) {
        Enrolment enrolment = enrolmentRepository.findById(enrolmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrolment " + enrolmentId + " was not found"));

        boolean isOwner = enrolment.getStudentId().equals(currentUser.id());
        boolean isAdmin = currentUser.role().equals("ADMIN");

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You can only cancel your own enrolments");
        }

        if (enrolment.getStatus().equals(STATUS_CANCELLED)) {
            throw new InvalidRequestException("Enrolment is already cancelled");
        }

        enrolment.setStatus(STATUS_CANCELLED);
        enrolmentRepository.save(enrolment);

        // Cancelling returns the seat to the course.
        Course course = courseService.findCourseOrThrow(enrolment.getCourseId());
        course.setEnrolledCount(Math.max(0, course.getEnrolledCount() - 1));
        courseService.save(course);

        logger.info("Enrolment id={} cancelled by user id={}", enrolmentId, currentUser.id());
    }

    private EnrolmentResponse toResponse(Enrolment enrolment) {
        return new EnrolmentResponse(
                enrolment.getId(),
                enrolment.getStudentId(),
                enrolment.getStudentName(),
                enrolment.getStudentEmail(),
                enrolment.getCourseId(),
                enrolment.getCourseTitle(),
                enrolment.getCourseCategory(),
                enrolment.getCourseLevel(),
                enrolment.getStatus(),
                enrolment.getEnrolledAt()
        );
    }
}
