package com.example.backend.service;

import com.example.backend.dto.CourseResponse;
import com.example.backend.dto.CreateCourseRequest;
import com.example.backend.dto.UpdateCourseRequest;
import com.example.backend.exception.InvalidRequestException;
import com.example.backend.model.Course;
import com.example.backend.repository.CourseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private CourseService courseService;

    private Course existingCourse() {
        Course course = new Course("Intro to Java", "desc", "Programming", "Beginner", "Mr. Tan", 10);
        course.setId("course-1");
        course.setEnrolledCount(5);
        return course;
    }

    private UpdateCourseRequest updateRequest(int capacity, String imageBase64) {
        UpdateCourseRequest request = new UpdateCourseRequest();
        request.setTitle("Intro to Java");
        request.setDescription("desc");
        request.setCategory("Programming");
        request.setLevel("Beginner");
        request.setInstructor("Mr. Tan");
        request.setCapacity(capacity);
        request.setImageBase64(imageBase64);
        return request;
    }

    @Test
    void updateCourseRejectsCapacityBelowCurrentEnrolledCount() {
        Course course = existingCourse();
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        UpdateCourseRequest request = updateRequest(2, null);

        assertThatThrownBy(() -> courseService.updateCourse("course-1", request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Capacity cannot be less than");
    }

    @Test
    void updateCourseAcceptsCapacityAtOrAboveEnrolledCount() {
        Course course = existingCourse();
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponse response = courseService.updateCourse("course-1", updateRequest(5, null));

        assertThat(response.getCapacity()).isEqualTo(5);
    }

    @Test
    void searchCoursesRejectsNegativePage() {
        assertThatThrownBy(() -> courseService.searchCourses(null, null, null, null, -1, 10, "title", "asc"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void searchCoursesRejectsSizeOverFifty() {
        assertThatThrownBy(() -> courseService.searchCourses(null, null, null, null, 0, 51, "title", "asc"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void searchCoursesRejectsDisallowedSortField() {
        assertThatThrownBy(() -> courseService.searchCourses(null, null, null, null, 0, 10, "password", "asc"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void searchCoursesRejectsInvalidDirection() {
        assertThatThrownBy(() -> courseService.searchCourses(null, null, null, null, 0, 10, "title", "sideways"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void updateCourseRejectsImageThatIsNotADataUri() {
        Course course = existingCourse();
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        UpdateCourseRequest request = updateRequest(10, "not-a-data-uri");

        assertThatThrownBy(() -> courseService.updateCourse("course-1", request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("data URI");
    }

    @Test
    void updateCourseRejectsImageOverTwoMegabytes() {
        Course course = existingCourse();
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        byte[] oversized = new byte[2 * 1024 * 1024 + 1];
        String imageBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(oversized);

        assertThatThrownBy(() -> courseService.updateCourse("course-1", updateRequest(10, imageBase64)))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("2MB");
    }

    @Test
    void updateCourseAcceptsValidSmallImage() {
        Course course = existingCourse();
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String imageBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString("tiny-image-bytes".getBytes());

        CourseResponse response = courseService.updateCourse("course-1", updateRequest(10, imageBase64));

        assertThat(response.getImageBase64()).isEqualTo(imageBase64);
    }

    @Test
    void createCourseTrimsFieldsAndDefaultsToActiveWithNoImage() {
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course saved = invocation.getArgument(0);
            saved.setId("course-2");
            return saved;
        });

        CreateCourseRequest request = new CreateCourseRequest();
        request.setTitle("  Advanced Spring  ");
        request.setDescription("desc");
        request.setCategory("Programming");
        request.setLevel("Advanced");
        request.setInstructor("Ms. Wong");
        request.setCapacity(20);

        CourseResponse response = courseService.createCourse(request);

        assertThat(response.getTitle()).isEqualTo("Advanced Spring");
        assertThat(response.isActive()).isTrue();
        assertThat(response.getEnrolledCount()).isZero();
        assertThat(response.getImageBase64()).isNull();
    }
}
