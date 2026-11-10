package com.example.lms.service;

import com.example.lms.BaseIntegrationTest;
import com.example.lms.entity.*;
import com.example.lms.entity.Module;
import com.example.lms.enums.Role;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@Transactional
class CourseServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired private CourseService courseService;
    @Autowired private CategoryService categoryService;
    @Autowired private UserService userService;
    @Autowired private AssignmentService assignmentService;
    @Autowired private CourseRepository courseRepository;
    @Autowired private ModuleRepository moduleRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private AssignmentRepository assignmentRepository;

    private User teacher;
    private Category category;

    @BeforeEach
    void setUp() {
        teacher = userService.createUser("Teacher", "t@example.com", Role.TEACHER);
        category = categoryService.create("Programming-" + System.nanoTime());
    }

    private Course newCourse() {
        return courseService.create("Course", "desc", "4w", LocalDate.now(),
                category.getId(), teacher.getId());
    }

    @Test
    @DisplayName("Create course with modules, lessons and assignments — persisted hierarchy")
    void createCourse_withHierarchy() {
        Course course = newCourse();

        Module m1 = courseService.addModule(course.getId(), "M1", 1, "first");
        Module m2 = courseService.addModule(course.getId(), "M2", 2, "second");

        Lesson l1 = courseService.addLesson(m1.getId(), "L1", "c", null);
        Lesson l2 = courseService.addLesson(m1.getId(), "L2", "c", null);
        Lesson l3 = courseService.addLesson(m2.getId(), "L3", "c", null);

        assignmentService.create(l1.getId(), "A1", "d", LocalDate.now().plusDays(7), 100);

        Course loaded = courseService.findWithStructure(course.getId());
        assertThat(loaded.getModules()).hasSize(2);
        assertThat(loaded.getModules().get(0).getTitle()).isEqualTo("M1");
        assertThat(loaded.getModules().get(0).getLessons()).hasSize(2);
        assertThat(loaded.getModules().get(1).getLessons()).hasSize(1);

        assertThat(lessonRepository.findByModuleIdOrderByIdAsc(m1.getId())).hasSize(2);
        assertThat(assignmentRepository.findByLessonId(l1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Update course — persisted")
    void updateCourse() {
        Course course = newCourse();

        courseService.update(course.getId(), "New title", "new desc", "8w", LocalDate.now().plusDays(1));

        Course reloaded = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("New title");
        assertThat(reloaded.getDescription()).isEqualTo("new desc");
        assertThat(reloaded.getDuration()).isEqualTo("8w");
    }

    @Test
    @DisplayName("Delete course — cascade to modules, lessons, assignments")
    void deleteCourse_cascade() {
        Course course = newCourse();
        Module m = courseService.addModule(course.getId(), "M", 1, null);
        Lesson l = courseService.addLesson(m.getId(), "L", "c", null);
        assignmentService.create(l.getId(), "A", "d", null, 10);

        Long courseId = course.getId();
        Long moduleId = m.getId();
        Long lessonId = l.getId();

        courseService.delete(courseId);

        assertThat(courseRepository.findById(courseId)).isEmpty();
        assertThat(moduleRepository.findById(moduleId)).isEmpty();
        assertThat(lessonRepository.findById(lessonId)).isEmpty();
        assertThat(assignmentRepository.findByLessonId(lessonId)).isEmpty();
    }

    @Test
    @DisplayName("findById for missing course throws ResourceNotFoundException")
    void findById_notFound() {
        assertThatThrownBy(() -> courseService.findById(999_999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course");
    }

    @Test
    @DisplayName("addTag to course — M-M persisted, duplicate tag → exception")
    void addTag() {
        Course course = newCourse();

        courseService.addTag(course.getId(), "Java");
        courseService.addTag(course.getId(), "Hibernate");

        Course reloaded = courseRepository.findById(course.getId()).orElseThrow();
        assertThat(reloaded.getTags()).hasSize(2);
        assertThat(reloaded.getTags()).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Java", "Hibernate");

        assertThatThrownBy(() -> courseService.addTag(course.getId(), "Java"))
                .isInstanceOf(com.example.lms.exception.DuplicateResourceException.class);
    }
}