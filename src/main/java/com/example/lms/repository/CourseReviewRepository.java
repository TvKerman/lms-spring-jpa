package com.example.lms.repository;

import com.example.lms.entity.CourseReview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    @EntityGraph(attributePaths = {"student", "course"})
    List<CourseReview> findByCourseId(Long courseId);

    @EntityGraph(attributePaths = {"student", "course"})
    List<CourseReview> findByStudentId(Long studentId);

    Optional<CourseReview> findByCourseIdAndStudentId(Long courseId, Long studentId);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    @Query("select avg(r.rating) from CourseReview r where r.course.id = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") Long courseId);
}