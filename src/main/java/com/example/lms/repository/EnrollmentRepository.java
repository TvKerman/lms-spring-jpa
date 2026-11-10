package com.example.lms.repository;

import com.example.lms.entity.Enrollment;
import com.example.lms.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    List<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    long countByCourseId(Long courseId);

    @EntityGraph(attributePaths = "course")
    List<Enrollment> findWithCourseByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"student", "course"})
    List<Enrollment> findWithStudentByCourseId(Long courseId);
}