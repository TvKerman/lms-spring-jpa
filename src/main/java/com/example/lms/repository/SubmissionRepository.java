package com.example.lms.repository;

import com.example.lms.entity.Submission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    @EntityGraph(attributePaths = {"student"})
    List<Submission> findByAssignmentId(Long assignmentId);

    List<Submission> findByStudentId(Long studentId);

    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    List<Submission> findByAssignmentIdAndScoreIsNull(Long assignmentId);

    @EntityGraph(attributePaths = {"student", "assignment"})
    @Query("select s from Submission s where s.id = :id")
    Optional<Submission> findWithDetailsById(@Param("id") Long id);
}