package com.example.backend.repository;

import com.example.backend.model.Enrolment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EnrolmentRepository extends MongoRepository<Enrolment, String> {

    List<Enrolment> findByStudentId(String studentId);

    Optional<Enrolment> findByStudentIdAndCourseIdAndStatus(String studentId, String courseId, String status);
}
