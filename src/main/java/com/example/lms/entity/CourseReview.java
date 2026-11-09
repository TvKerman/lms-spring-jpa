package com.example.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "course_review",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_course_review_course_student",
                columnNames = {"course_id", "student_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"id", "rating", "createdAt"})
public class CourseReview extends BaseEntity {

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
}