package com.example.backend.service;

import com.example.backend.dto.ReportCountResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * EnrolmentReportService
 * -----------------------
 * MongoDB aggregation pipelines for the admin dashboard. All three reports
 * only count ACTIVE enrolments, so a cancelled enrolment doesn't inflate
 * "how many students are currently in this course" - a cancelled seat was
 * already returned to the course by EnrolmentService.
 */
@Service
public class EnrolmentReportService {

    private static final Logger logger = LoggerFactory.getLogger(EnrolmentReportService.class);

    private static final String COLLECTION = "enrolments";

    private final MongoTemplate mongoTemplate;

    public EnrolmentReportService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // Doubles as "most popular courses" - the list is already sorted by
    // enrolment count, so the top entries are the most popular.
    public List<ReportCountResponse> countEnrolmentsByCourse() {
        logger.info("Generating enrolments-by-course report");
        return countActiveEnrolmentsGroupedBy("courseTitle", Sort.Direction.DESC);
    }

    public List<ReportCountResponse> countEnrolmentsByCategory() {
        logger.info("Generating enrolments-by-category report");
        return countActiveEnrolmentsGroupedBy("courseCategory", Sort.Direction.DESC);
    }

    public List<ReportCountResponse> countEnrolmentsByMonth() {
        logger.info("Generating monthly enrolment totals report");

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").is("ACTIVE")),
                Aggregation.project()
                        .and(DateOperators.DateToString.dateOf("enrolledAt").toString("%Y-%m"))
                        .as("month"),
                Aggregation.group("month").count().as("count"),
                Aggregation.project("count").and("_id").as("label"),
                Aggregation.sort(Sort.Direction.ASC, "label")
        );

        AggregationResults<ReportCountResponse> results =
                mongoTemplate.aggregate(aggregation, COLLECTION, ReportCountResponse.class);

        return results.getMappedResults();
    }

    private List<ReportCountResponse> countActiveEnrolmentsGroupedBy(String fieldName, Sort.Direction direction) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("status").is("ACTIVE")),
                Aggregation.group(fieldName).count().as("count"),
                Aggregation.project("count").and("_id").as("label"),
                Aggregation.sort(direction, "count")
        );

        AggregationResults<ReportCountResponse> results =
                mongoTemplate.aggregate(aggregation, COLLECTION, ReportCountResponse.class);

        return results.getMappedResults();
    }
}
