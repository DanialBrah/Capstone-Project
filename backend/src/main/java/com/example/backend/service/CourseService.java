package com.example.backend.service;

import com.example.backend.dto.CourseResponse;
import com.example.backend.dto.CreateCourseRequest;
import com.example.backend.dto.UpdateCourseRequest;
import com.example.backend.exception.InvalidRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.Course;
import com.example.backend.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/*
 * CourseService
 * -------------
 * Course is the "main entity". Search/filter/sort/pagination are combined
 * in one query using MongoDB Criteria built up dynamically - only the
 * filters the caller actually supplied get added, so /api/courses works
 * whether you pass none, one, or all of them at once.
 */
@Service
public class CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "title", "category", "level", "capacity", "createdAt"
    );

    private static final Pattern IMAGE_DATA_URI_PATTERN =
            Pattern.compile("^data:image/(png|jpe?g|webp|gif);base64,(.+)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final int MAX_IMAGE_BYTES = 2 * 1024 * 1024;

    private final CourseRepository courseRepository;
    private final MongoTemplate mongoTemplate;

    public CourseService(CourseRepository courseRepository, MongoTemplate mongoTemplate) {
        this.courseRepository = courseRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Page<CourseResponse> searchCourses(
            String keyword, String category, String level, Boolean active,
            int page, int size, String sortBy, String direction) {

        logger.info("Searching courses keyword={}, category={}, level={}, active={}, page={}, size={}, sortBy={}, direction={}",
                keyword, category, level, active, page, size, sortBy, direction);

        validatePageRequest(page, size, sortBy, direction);

        List<Criteria> criteria = new ArrayList<>();

        if (hasValue(keyword)) {
            criteria.add(Criteria.where("title").regex(Pattern.quote(keyword.trim()), "i"));
        }
        if (hasValue(category)) {
            criteria.add(Criteria.where("category").regex("^" + Pattern.quote(category.trim()) + "$", "i"));
        }
        if (hasValue(level)) {
            criteria.add(Criteria.where("level").regex("^" + Pattern.quote(level.trim()) + "$", "i"));
        }
        if (active != null) {
            criteria.add(Criteria.where("active").is(active));
        }

        Query query = new Query();
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Course.class);

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        List<Course> courses = mongoTemplate.find(query.with(pageRequest), Course.class);
        List<CourseResponse> responses = courses.stream().map(this::toResponse).toList();

        return new PageImpl<>(responses, pageRequest, total);
    }

    public CourseResponse getCourseById(String id) {
        return toResponse(findCourseOrThrow(id));
    }

    public CourseResponse createCourse(CreateCourseRequest request) {
        validateImage(request.getImageBase64());

        Course course = new Course(
                request.getTitle().trim(),
                request.getDescription().trim(),
                request.getCategory().trim(),
                request.getLevel().trim(),
                request.getInstructor().trim(),
                request.getCapacity()
        );
        course.setImageBase64(hasValue(request.getImageBase64()) ? request.getImageBase64() : null);

        Course savedCourse = courseRepository.save(course);
        logger.info("Created course id={} title={}", savedCourse.getId(), savedCourse.getTitle());

        return toResponse(savedCourse);
    }

    public CourseResponse updateCourse(String id, UpdateCourseRequest request) {
        Course course = findCourseOrThrow(id);
        validateImage(request.getImageBase64());

        if (request.getCapacity() < course.getEnrolledCount()) {
            throw new InvalidRequestException(
                    "Capacity cannot be less than the current enrolled count (" + course.getEnrolledCount() + ")");
        }

        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription().trim());
        course.setCategory(request.getCategory().trim());
        course.setLevel(request.getLevel().trim());
        course.setInstructor(request.getInstructor().trim());
        course.setCapacity(request.getCapacity());
        course.setImageBase64(hasValue(request.getImageBase64()) ? request.getImageBase64() : null);

        Course savedCourse = courseRepository.save(course);
        logger.info("Updated course id={}", savedCourse.getId());

        return toResponse(savedCourse);
    }

    public CourseResponse setActive(String id, boolean active) {
        Course course = findCourseOrThrow(id);
        course.setActive(active);

        Course savedCourse = courseRepository.save(course);
        logger.info("Set course id={} active={}", savedCourse.getId(), active);

        return toResponse(savedCourse);
    }

    Course findCourseOrThrow(String id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course " + id + " was not found"));
    }

    void save(Course course) {
        courseRepository.save(course);
    }

    private void validatePageRequest(int page, int size, String sortBy, String direction) {
        if (page < 0) {
            throw new InvalidRequestException("Page must be zero or greater");
        }
        if (size < 1 || size > 50) {
            throw new InvalidRequestException("Size must be between 1 and 50");
        }
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new InvalidRequestException("Sort field is not allowed: " + sortBy);
        }
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw new InvalidRequestException("Direction must be either asc or desc");
        }
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    // Only ever called with a value the caller intends to store - blank/null
    // (meaning "no image" or "clear the image") is left to the caller.
    private void validateImage(String imageBase64) {
        if (!hasValue(imageBase64)) {
            return;
        }

        var matcher = IMAGE_DATA_URI_PATTERN.matcher(imageBase64.trim());
        if (!matcher.matches()) {
            throw new InvalidRequestException(
                    "Image must be a base64 data URI (data:image/png|jpeg|webp|gif;base64,...)");
        }

        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(matcher.group(2));
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Image data is not valid base64");
        }

        if (decoded.length > MAX_IMAGE_BYTES) {
            throw new InvalidRequestException("Image must be 2MB or smaller");
        }
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCategory(),
                course.getLevel(),
                course.getInstructor(),
                course.getCapacity(),
                course.getEnrolledCount(),
                course.isActive(),
                course.getImageBase64()
        );
    }
}
