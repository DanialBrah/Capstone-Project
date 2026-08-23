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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrolmentServiceTest {

    @Mock
    private EnrolmentRepository enrolmentRepository;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private EnrolmentService enrolmentService;

    private static final AuthenticatedUser STUDENT = new AuthenticatedUser("student-1", "Ada", "ada@example.com", "STUDENT");
    private static final AuthenticatedUser ADMIN = new AuthenticatedUser("admin-1", "Admin", "admin@example.com", "ADMIN");
    private static final AuthenticatedUser OTHER_STUDENT = new AuthenticatedUser("student-2", "Bob", "bob@example.com", "STUDENT");

    private Course activeCourse(int capacity, int enrolledCount) {
        Course course = new Course("Intro to Java", "desc", "Programming", "Beginner", "Mr. Tan", capacity);
        course.setId("course-1");
        course.setEnrolledCount(enrolledCount);
        course.setActive(true);
        return course;
    }

    @Test
    void enrolRejectsInactiveCourse() {
        Course course = activeCourse(10, 0);
        course.setActive(false);
        when(courseService.findCourseOrThrow("course-1")).thenReturn(course);

        CreateEnrolmentRequest request = new CreateEnrolmentRequest();
        request.setCourseId("course-1");

        assertThatThrownBy(() -> enrolmentService.enrol(STUDENT, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void enrolRejectsFullCourse() {
        Course course = activeCourse(2, 2);
        when(courseService.findCourseOrThrow("course-1")).thenReturn(course);

        CreateEnrolmentRequest request = new CreateEnrolmentRequest();
        request.setCourseId("course-1");

        assertThatThrownBy(() -> enrolmentService.enrol(STUDENT, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("full");
    }

    @Test
    void enrolRejectsDuplicateActiveEnrolment() {
        Course course = activeCourse(10, 1);
        when(courseService.findCourseOrThrow("course-1")).thenReturn(course);
        when(enrolmentRepository.findByStudentIdAndCourseIdAndStatus("student-1", "course-1", "ACTIVE"))
                .thenReturn(Optional.of(new Enrolment()));

        CreateEnrolmentRequest request = new CreateEnrolmentRequest();
        request.setCourseId("course-1");

        assertThatThrownBy(() -> enrolmentService.enrol(STUDENT, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void enrolSucceedsAndIncrementsCourseSeatCount() {
        Course course = activeCourse(10, 3);
        when(courseService.findCourseOrThrow("course-1")).thenReturn(course);
        when(enrolmentRepository.findByStudentIdAndCourseIdAndStatus("student-1", "course-1", "ACTIVE"))
                .thenReturn(Optional.empty());
        when(enrolmentRepository.save(any(Enrolment.class))).thenAnswer(invocation -> {
            Enrolment saved = invocation.getArgument(0);
            saved.setId("enrolment-1");
            return saved;
        });

        CreateEnrolmentRequest request = new CreateEnrolmentRequest();
        request.setCourseId("course-1");

        EnrolmentResponse response = enrolmentService.enrol(STUDENT, request);

        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getStudentId()).isEqualTo("student-1");
        assertThat(course.getEnrolledCount()).isEqualTo(4);
        verify(courseService).save(course);
    }

    @Test
    void cancelRejectsNonOwnerNonAdmin() {
        Enrolment enrolment = new Enrolment();
        enrolment.setId("enrolment-1");
        enrolment.setStudentId("student-1");
        enrolment.setStatus("ACTIVE");

        when(enrolmentRepository.findById("enrolment-1")).thenReturn(Optional.of(enrolment));

        assertThatThrownBy(() -> enrolmentService.cancel(OTHER_STUDENT, "enrolment-1"))
                .isInstanceOf(AccessDeniedException.class);

        verify(enrolmentRepository, never()).save(any());
    }

    @Test
    void cancelAllowsAdminToCancelAnyEnrolment() {
        Enrolment enrolment = new Enrolment();
        enrolment.setId("enrolment-1");
        enrolment.setStudentId("student-1");
        enrolment.setCourseId("course-1");
        enrolment.setStatus("ACTIVE");

        Course course = activeCourse(10, 1);

        when(enrolmentRepository.findById("enrolment-1")).thenReturn(Optional.of(enrolment));
        when(courseService.findCourseOrThrow("course-1")).thenReturn(course);

        enrolmentService.cancel(ADMIN, "enrolment-1");

        assertThat(enrolment.getStatus()).isEqualTo("CANCELLED");
        assertThat(course.getEnrolledCount()).isZero();
    }

    @Test
    void cancelRejectsAlreadyCancelledEnrolment() {
        Enrolment enrolment = new Enrolment();
        enrolment.setId("enrolment-1");
        enrolment.setStudentId("student-1");
        enrolment.setStatus("CANCELLED");

        when(enrolmentRepository.findById("enrolment-1")).thenReturn(Optional.of(enrolment));

        assertThatThrownBy(() -> enrolmentService.cancel(STUDENT, "enrolment-1"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void cancelThrowsWhenEnrolmentDoesNotExist() {
        when(enrolmentRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrolmentService.cancel(STUDENT, "missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelNeverDecrementsSeatCountBelowZero() {
        Enrolment enrolment = new Enrolment();
        enrolment.setId("enrolment-1");
        enrolment.setStudentId("student-1");
        enrolment.setCourseId("course-1");
        enrolment.setStatus("ACTIVE");

        Course course = activeCourse(10, 0);

        when(enrolmentRepository.findById("enrolment-1")).thenReturn(Optional.of(enrolment));
        when(courseService.findCourseOrThrow("course-1")).thenReturn(course);

        enrolmentService.cancel(STUDENT, "enrolment-1");

        assertThat(course.getEnrolledCount()).isZero();
    }
}
