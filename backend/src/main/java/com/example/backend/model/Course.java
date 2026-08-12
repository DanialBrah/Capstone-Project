package com.example.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    @Indexed
    private String title;

    private String description;

    @Indexed
    private String category;

    @Indexed
    private String level;

    private String instructor;

    private int capacity;

    private int enrolledCount;

    @Indexed
    private boolean active;

    private Instant createdAt;

    public Course() {
    }

    public Course(String title, String description, String category, String level,
                  String instructor, int capacity) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.level = level;
        this.instructor = instructor;
        this.capacity = capacity;
        this.enrolledCount = 0;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
