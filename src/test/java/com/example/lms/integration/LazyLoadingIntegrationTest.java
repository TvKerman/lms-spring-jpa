package com.example.lms.integration;

import com.example.lms.BaseIntegrationTest;
import com.example.lms.entity.Course;
import com.example.lms.entity.User;
import com.example.lms.enums.Role;
import com.example.lms.repository.CourseRepository;
import com.example.lms.service.CategoryService;
import com.example.lms.service.CourseService;
import com.example.lms.service.UserService;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class LazyLoadingIntegrationTest extends BaseIntegrationTest {

    @Autowired private CourseRepository courseRepository;
    @Autowired private CourseService courseService;
    @Autowired private CategoryService categoryService;
    @Autowired private UserService userService;
    @Autowired private TransactionTemplate txTemplate;

    private Long courseId;

    @BeforeEach
    void setUp() {
        courseId = txTemplate.execute(status -> {
            User teacher = userService.createUser(
                    "T", "t-" + System.nanoTime() + "@ex.com", Role.TEACHER);
            var cat = categoryService.create("Cat-" + System.nanoTime());
            Course course = courseService.create(
                    "C", "d", "1w", LocalDate.now(), cat.getId(), teacher.getId());
            courseService.addModule(course.getId(), "M1", 1, null);
            courseService.addModule(course.getId(), "M2", 2, null);
            return course.getId();
        });
    }

    @Test
    @DisplayName("Lazy loading: accessing modules outside session → LazyInitializationException")
    void lazyLoading_throwsOutsideSession() {
        Course course = courseRepository.findById(courseId).orElseThrow();

        assertThatThrownBy(() -> course.getModules().size())
                .isInstanceOf(LazyInitializationException.class);
    }

    @Test
    @DisplayName("EntityGraph: findWithModulesById loads modules eagerly — no exception")
    void entityGraph_avoidsLazyException() {
        Course course = courseRepository.findWithModulesById(courseId).orElseThrow();

        assertThat(course.getModules()).hasSize(2);
    }
}