package com.example.backend.dto;

import java.time.Instant;

public class EnrolmentResponse {

    private String id;
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String courseId;
    private String courseTitle;
    private String courseCategory;
    private String courseLevel;
    private String status;
    private Instant enrolledAt;

    public EnrolmentResponse(String id, String studentId, String studentName, String studentEmail,
                              String courseId, String courseTitle, String courseCategory, String courseLevel,
                              String status, Instant enrolledAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.courseCategory = courseCategory;
        this.courseLevel = courseLevel;
        this.status = status;
        this.enrolledAt = enrolledAt;
    }

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public String getCourseId() { return courseId; }
    public String getCourseTitle() { return courseTitle; }
    public String getCourseCategory() { return courseCategory; }
    public String getCourseLevel() { return courseLevel; }
    public String getStatus() { return status; }
    public Instant getEnrolledAt() { return enrolledAt; }
}
