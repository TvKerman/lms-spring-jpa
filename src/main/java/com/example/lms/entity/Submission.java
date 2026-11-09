package com.example.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "submission",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_submission_assignment_student",
                columnNames = {"assignment_id", "student_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"id", "submittedAt", "score"})
public class Submission extends BaseEntity {

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer score;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
}