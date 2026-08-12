package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateEnrolmentRequest {

    @NotBlank(message = "Course id is required")
    private String courseId;

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
}
