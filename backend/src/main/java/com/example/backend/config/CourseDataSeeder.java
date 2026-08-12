package com.example.backend.config;

import com.example.backend.model.Course;
import com.example.backend.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CourseDataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(CourseDataSeeder.class);

    @Bean
    CommandLineRunner seedCourses(CourseRepository courseRepository) {
        return args -> {
            if (courseRepository.count() > 0) {
                logger.info("Courses already seeded, skipping");
                return;
            }

            courseRepository.saveAll(java.util.List.of(
                    new Course("Introduction to Java", "Core Java syntax, OOP basics, and collections.",
                            "Programming", "Beginner", "Mr. Tan", 30),
                    new Course("Full-Stack Web Development", "React front end with a Spring Boot REST API.",
                            "Programming", "Intermediate", "Ms. Lee", 25),
                    new Course("MongoDB for Developers", "Document modelling, indexing, and aggregation pipelines.",
                            "Database", "Intermediate", "Mr. Kumar", 20),
                    new Course("UX Design Fundamentals", "User research, wireframing, and usability testing.",
                            "Design", "Beginner", "Ms. Wong", 15),
                    new Course("Advanced Spring Security", "JWT auth, role-based access, and OAuth2.",
                            "Programming", "Advanced", "Mr. Tan", 10)
            ));

            logger.info("Seeded 5 sample courses");
        };
    }
}
