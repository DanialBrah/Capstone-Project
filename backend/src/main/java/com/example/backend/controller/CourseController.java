package com.example.backend.controller;

import com.example.backend.dto.CourseResponse;
import com.example.backend.dto.CreateCourseRequest;
import com.example.backend.dto.UpdateCourseRequest;
import com.example.backend.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@Tag(name = "Courses", description = "Course catalog. Reads need any logged-in user; writes need ADMIN.")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // GET /api/courses?keyword=&category=&level=&active=&page=&size=&sortBy=&direction=
    @GetMapping
    @Operation(summary = "Search courses", description = "Paginated, filterable, sortable course search. sortBy: title|category|level|capacity|createdAt.")
    public Page<CourseResponse> searchCourses(
            @Parameter(description = "Case-insensitive match against title") @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @Parameter(description = "Filter by active/inactive status") @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Max 50") @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @Parameter(description = "asc or desc") @RequestParam(defaultValue = "asc") String direction) {

        return courseService.searchCourses(keyword, category, level, active, page, size, sortBy, direction);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a course by id")
    public CourseResponse getCourseById(@PathVariable String id) {
        return courseService.getCourseById(id);
    }

    @PostMapping
    @Operation(summary = "Create a course", description = "ADMIN only.")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseResponse created = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a course", description = "ADMIN only. Rejected if the new capacity is below the current enrolled count.")
    public CourseResponse updateCourse(@PathVariable String id, @Valid @RequestBody UpdateCourseRequest request) {
        return courseService.updateCourse(id, request);
    }

    // Soft delete: courses with existing enrolments can't be removed
    // outright without orphaning those records, so admins deactivate
    // instead. Deactivated courses are excluded from student browsing via
    // the active filter and rejected by EnrolmentService at enrol time.
    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a course", description = "ADMIN only. Soft delete - blocks new enrolments without deleting existing records.")
    public CourseResponse deactivateCourse(@PathVariable String id) {
        return courseService.setActive(id, false);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Reactivate a course", description = "ADMIN only.")
    public CourseResponse activateCourse(@PathVariable String id) {
        return courseService.setActive(id, true);
    }

    // Hard delete: only allowed when the course has no enrolment history at
    // all - see CourseService.deleteCourse. Anything with history should be
    // deactivated instead, via PATCH /{id}/deactivate.
    @DeleteMapping("/{id}")
    @Operation(summary = "Permanently delete a course", description = "ADMIN only. Rejected (400) if any enrolment - "
            + "even a cancelled one - references this course; deactivate it instead in that case.")
    public ResponseEntity<Void> deleteCourse(@PathVariable String id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
