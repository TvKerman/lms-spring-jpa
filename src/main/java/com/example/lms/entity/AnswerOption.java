package com.example.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "answer_option")
@Getter
@Setter
@NoArgsConstructor
@ToString(of = {"id", "text", "correct"})
public class AnswerOption extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String text;

    @Column(nullable = false)
    private boolean correct;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
}