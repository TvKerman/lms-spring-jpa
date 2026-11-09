package com.example.lms.entity;

import com.example.lms.enums.EnrollmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(
        name = "enrollment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_enrollment_user_course",
                columnNames = {"user_id", "course_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"id", "enrollDate", "status"})
public class Enrollment extends BaseEntity {

    @Column(name = "enroll_date", nullable = false)
    private LocalDate enrollDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}