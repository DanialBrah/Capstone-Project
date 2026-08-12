package com.example.backend.dto;

public class CourseResponse {

    private String id;
    private String title;
    private String description;
    private String category;
    private String level;
    private String instructor;
    private int capacity;
    private int enrolledCount;
    private boolean active;

    public CourseResponse(String id, String title, String description, String category, String level,
                           String instructor, int capacity, int enrolledCount, boolean active) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.level = level;
        this.instructor = instructor;
        this.capacity = capacity;
        this.enrolledCount = enrolledCount;
        this.active = active;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getLevel() { return level; }
    public String getInstructor() { return instructor; }
    public int getCapacity() { return capacity; }
    public int getEnrolledCount() { return enrolledCount; }
    public boolean isActive() { return active; }
}
