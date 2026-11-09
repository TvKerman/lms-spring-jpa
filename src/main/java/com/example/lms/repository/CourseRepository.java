package com.example.lms.repository;

import com.example.lms.entity.Course;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByCategoryId(Long categoryId);

    List<Course> findByTeacherId(Long teacherId);

    List<Course> findByTitleContainingIgnoreCase(String titlePart);

    @EntityGraph(attributePaths = "modules")
    Optional<Course> findWithModulesById(Long id);
}