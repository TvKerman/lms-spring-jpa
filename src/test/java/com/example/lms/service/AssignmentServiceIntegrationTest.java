package com.example.lms.service;

import com.example.lms.BaseIntegrationTest;
import com.example.lms.entity.*;
import com.example.lms.entity.Module;
import com.example.lms.enums.Role;
import com.example.lms.exception.BusinessRuleException;
import com.example.lms.exception.DuplicateResourceException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@Transactional
class AssignmentServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired private AssignmentService assignmentService;
    @Autowired private SubmissionRepository submissionRepository;
    @Autowired private UserService userService;
    @Autowired private CourseService courseService;
    @Autowired private CategoryService categoryService;

    private User student;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
        User teacher = userService.createUser("T", "t-" + System.nanoTime() + "@ex.com", Role.TEACHER);
        student = userService.createUser("S", "s-" + System.nanoTime() + "@ex.com", Role.STUDENT);
        var cat = categoryService.create("Cat-" + System.nanoTime());
        Course course = courseService.create("C", "d", "1w", LocalDate.now(),
                cat.getId(), teacher.getId());
        Module m = courseService.addModule(course.getId(), "M", 1, null);
        Lesson l = courseService.addLesson(m.getId(), "L", "c", null);
        assignment = assignmentService.create(l.getId(), "A",
                "d", LocalDate.now().plusDays(7), 100);
    }

    @Test
    @DisplayName("Submit within deadline → SUBMITTED, no score")
    void submit_success() {
        Submission s = assignmentService.submit(assignment.getId(), student.getId(), "my answer");

        assertThat(s.getId()).isNotNull();
        assertThat(s.getScore()).isNull();
        assertThat(s.getSubmittedAt()).isNotNull();
        assertThat(submissionRepository.existsByAssignmentIdAndStudentId(
                assignment.getId(), student.getId())).isTrue();
    }

    @Test
    @DisplayName("Submit twice → DuplicateResourceException")
    void submit_duplicate() {
        assignmentService.submit(assignment.getId(), student.getId(), "a1");

        assertThatThrownBy(() ->
                assignmentService.submit(assignment.getId(), student.getId(), "a2"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Submit after deadline → BusinessRuleException")
    void submit_afterDeadline() {
        User teacher = userService.createUser("T2", "t2-" + System.nanoTime() + "@ex.com", Role.TEACHER);
        var cat = categoryService.create("Cat2-" + System.nanoTime());
        Course course = courseService.create("C2", "d", "1w", LocalDate.now(),
                cat.getId(), teacher.getId());
        Module m = courseService.addModule(course.getId(), "M", 1, null);
        Lesson l = courseService.addLesson(m.getId(), "L", "c", null);
        Assignment expired = assignmentService.create(l.getId(), "A",
                "d", LocalDate.now().minusDays(1), 50);

        assertThatThrownBy(() ->
                assignmentService.submit(expired.getId(), student.getId(), "late"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Deadline");
    }

    @Test
    @DisplayName("Grade submission → score and feedback persisted")
    void grade_success() {
        Submission s = assignmentService.submit(assignment.getId(), student.getId(), "ans");

        Submission graded = assignmentService.grade(s.getId(), 85, "good");

        assertThat(graded.getScore()).isEqualTo(85);
        assertThat(graded.getFeedback()).isEqualTo("good");
    }

    @Test
    @DisplayName("Grade with score out of range → BusinessRuleException")
    void grade_outOfRange() {
        Submission s = assignmentService.submit(assignment.getId(), student.getId(), "ans");

        assertThatThrownBy(() -> assignmentService.grade(s.getId(), 150, "too high"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("0 and 100");

        assertThatThrownBy(() -> assignmentService.grade(s.getId(), -1, "negative"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("Grade missing submission → ResourceNotFoundException")
    void grade_notFound() {
        assertThatThrownBy(() -> assignmentService.grade(999_999L, 10, "x"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}