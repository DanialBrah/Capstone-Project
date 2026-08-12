package com.example.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/*
 * Enrolment
 * ---------
 * The "action/transaction" entity. studentName/studentEmail/courseTitle
 * are snapshotted at enrolment time rather than looked up fresh each time
 * - this is a deliberate denormalization: listing enrolments (a student's
 * own, or the admin's full list) never needs to join against User or
 * Course, at the cost of showing slightly stale names if a user/course is
 * later renamed. For a transaction record, keeping "what it looked like
 * when it happened" is usually the more correct behaviour anyway.
 */
@Document(collection = "enrolments")
public class Enrolment {

    @Id
    private String id;

    @Indexed
    private String studentId;

    private String studentName;
    private String studentEmail;

    @Indexed
    private String courseId;

    private String courseTitle;

    @Indexed
    private String courseCategory;

    private String courseLevel;

    @Indexed
    private String status;

    private Instant enrolledAt;

    public Enrolment() {
    }

    public Enrolment(String studentId, String studentName, String studentEmail,
                      String courseId, String courseTitle, String courseCategory,
                      String courseLevel, String status) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.courseCategory = courseCategory;
        this.courseLevel = courseLevel;
        this.status = status;
        this.enrolledAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public String getCourseCategory() { return courseCategory; }
    public void setCourseCategory(String courseCategory) { this.courseCategory = courseCategory; }
    public String getCourseLevel() { return courseLevel; }
    public void setCourseLevel(String courseLevel) { this.courseLevel = courseLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }
}
