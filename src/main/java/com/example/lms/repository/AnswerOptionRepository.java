package com.example.lms.repository;

import com.example.lms.entity.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {

    List<AnswerOption> findByQuestionId(Long questionId);

    List<AnswerOption> findByQuestionIdAndCorrectTrue(Long questionId);
}