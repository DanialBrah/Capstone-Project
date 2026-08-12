package com.example.backend.controller;

import com.example.backend.dto.CourseResponse;
import com.example.backend.dto.CreateCourseRequest;
import com.example.backend.dto.UpdateCourseRequest;
import com.example.backend.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * CourseController
 * -----------------
 * GET endpoints are open to any logged-in user (students browse courses).
 * Create/update/activate/deactivate are ADMIN-only - enforced in
 * SecurityConfig, not here, so the controller stays focused on HTTP
 * plumbing.
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // GET /api/courses?keyword=&category=&level=&active=&page=&size=&sortBy=&direction=
    @GetMapping
    public Page<CourseResponse> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        return courseService.searchCourses(keyword, category, level, active, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    public CourseResponse getCourseById(@PathVariable String id) {
        return courseService.getCourseById(id);
    }

    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseResponse created = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public CourseResponse updateCourse(@PathVariable String id, @Valid @RequestBody UpdateCourseRequest request) {
        return courseService.updateCourse(id, request);
    }

    // Soft delete: courses with existing enrolments can't be removed
    // outright without orphaning those records, so admins deactivate
    // instead. Deactivated courses are excluded from student browsing via
    // the active filter and rejected by EnrolmentService at enrol time.
    @PatchMapping("/{id}/deactivate")
    public CourseResponse deactivateCourse(@PathVariable String id) {
        return courseService.setActive(id, false);
    }

    @PatchMapping("/{id}/activate")
    public CourseResponse activateCourse(@PathVariable String id) {
        return courseService.setActive(id, true);
    }
}
