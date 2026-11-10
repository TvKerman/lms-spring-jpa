package com.example.lms.service;

import com.example.lms.BaseIntegrationTest;
import com.example.lms.entity.Course;
import com.example.lms.entity.Enrollment;
import com.example.lms.entity.User;
import com.example.lms.enums.EnrollmentStatus;
import com.example.lms.enums.Role;
import com.example.lms.exception.BusinessRuleException;
import com.example.lms.exception.DuplicateResourceException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
class EnrollmentServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired private EnrollmentService enrollmentService;
    @Autowired private EnrollmentRepository enrollmentRepository;
    @Autowired private UserService userService;
    @Autowired private CourseService courseService;
    @Autowired private CategoryService categoryService;

    private User teacher;
    private User student;
    private Course course;

    @BeforeEach
    void setUp() {
        teacher = userService.createUser("Teacher", "t@example.com", Role.TEACHER);
        student = userService.createUser("Student", "s@example.com", Role.STUDENT);
        var cat = categoryService.create("Cat-" + System.nanoTime());
        course = courseService.create("C", "d", "1w", LocalDate.now(),
                cat.getId(), teacher.getId());
    }

    @Test
    @DisplayName("Enroll student → ACTIVE enrollment created")
    void enroll_success() {
        Enrollment e = enrollmentService.enroll(student.getId(), course.getId());

        assertThat(e.getId()).isNotNull();
        assertThat(e.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(e.getEnrollDate()).isEqualTo(LocalDate.now());
        assertThat(enrollmentRepository.existsByStudentIdAndCourseId(student.getId(), course.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("Enroll duplicate → DuplicateResourceException")
    void enroll_duplicate() {
        enrollmentService.enroll(student.getId(), course.getId());

        assertThatThrownBy(() -> enrollmentService.enroll(student.getId(), course.getId()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Enroll a TEACHER → BusinessRuleException")
    void enroll_nonStudent() {
        assertThatThrownBy(() -> enrollmentService.enroll(teacher.getId(), course.getId()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("STUDENT");
    }

    @Test
    @DisplayName("Unenroll removes enrollment")
    void unenroll() {
        enrollmentService.enroll(student.getId(), course.getId());
        enrollmentService.unenroll(student.getId(), course.getId());

        assertThat(enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("Unenroll non-existing → ResourceNotFoundException")
    void unenroll_notFound() {
        assertThatThrownBy(() -> enrollmentService.unenroll(student.getId(), course.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getCourseEnrollments returns students with preloaded User")
    void getCourseEnrollments() {
        User student2 = userService.createUser("S2", "s2@example.com", Role.STUDENT);
        enrollmentService.enroll(student.getId(), course.getId());
        enrollmentService.enroll(student2.getId(), course.getId());

        List<Enrollment> enrollments = enrollmentService.getCourseEnrollments(course.getId());

        assertThat(enrollments).hasSize(2);
        assertThat(enrollments).allSatisfy(e ->
                assertThat(e.getStudent().getName()).isNotBlank());
    }
}