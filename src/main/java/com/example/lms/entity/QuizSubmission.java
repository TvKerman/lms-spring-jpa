package com.example.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "quiz_submission",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_quiz_submission_quiz_student",
                columnNames = {"quiz_id", "student_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"id", "score", "takenAt"})
public class QuizSubmission extends BaseEntity {

    @Column(nullable = false)
    private Integer score;

    @Column(name = "taken_at", nullable = false)
    private LocalDateTime takenAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
}