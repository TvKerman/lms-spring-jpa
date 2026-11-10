package com.example.lms.service;

import com.example.lms.BaseIntegrationTest;
import com.example.lms.entity.*;
import com.example.lms.entity.Module;
import com.example.lms.enums.QuestionType;
import com.example.lms.enums.Role;
import com.example.lms.exception.BusinessRuleException;
import com.example.lms.exception.DuplicateResourceException;
import com.example.lms.repository.QuizSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@Transactional
class QuizServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired private QuizService quizService;
    @Autowired private QuizSubmissionRepository quizSubmissionRepository;
    @Autowired private UserService userService;
    @Autowired private CourseService courseService;
    @Autowired private CategoryService categoryService;

    private User student;
    private Quiz quiz;
    private Question q1;
    private Question q2;
    private Question q3;

    @BeforeEach
    void setUp() {
        User teacher = userService.createUser("T", "t-" + System.nanoTime() + "@ex.com", Role.TEACHER);
        student = userService.createUser("S", "s-" + System.nanoTime() + "@ex.com", Role.STUDENT);
        var cat = categoryService.create("Cat-" + System.nanoTime());
        Course course = courseService.create("C", "d", "1w", LocalDate.now(),
                cat.getId(), teacher.getId());
        Module module = courseService.addModule(course.getId(), "M", 1, null);

        quiz = quizService.create(module.getId(), "Quiz 1", 15);

        q1 = quizService.addQuestion(quiz.getId(), "Q1?",
                QuestionType.SINGLE_CHOICE,
                List.of(new QuizService.OptionInput("right", true),
                        new QuizService.OptionInput("wrong", false)));

        q2 = quizService.addQuestion(quiz.getId(), "Q2?",
                QuestionType.MULTIPLE_CHOICE,
                List.of(new QuizService.OptionInput("a", true),
                        new QuizService.OptionInput("b", true),
                        new QuizService.OptionInput("c", false)));

        q3 = quizService.addQuestion(quiz.getId(), "Q3?",
                QuestionType.SINGLE_CHOICE,
                List.of(new QuizService.OptionInput("yes", true),
                        new QuizService.OptionInput("no",  false)));
    }

    private Map<Long, List<Long>> allCorrectAnswers() {
        Map<Long, List<Long>> answers = new HashMap<>();
        for (Question q : List.of(q1, q2, q3)) {
            answers.put(q.getId(),
                    quizService.getOptions(q.getId()).stream()
                            .filter(AnswerOption::isCorrect)
                            .map(AnswerOption::getId)
                            .toList());
        }
        return answers;
    }

    @Test
    @DisplayName("Take quiz with all correct → score = 3")
    void take_allCorrect() {
        QuizSubmission result = quizService.take(quiz.getId(), student.getId(), allCorrectAnswers());

        assertThat(result.getScore()).isEqualTo(3);
        assertThat(result.getTakenAt()).isNotNull();
        assertThat(quizSubmissionRepository.existsByQuizIdAndStudentId(
                quiz.getId(), student.getId())).isTrue();
    }

    @Test
    @DisplayName("Take quiz with some wrong → score < 3")
    void take_someWrong() {
        Map<Long, List<Long>> answers = allCorrectAnswers();
        Long wrongOption = quizService.getOptions(q1.getId()).stream()
                .filter(o -> !o.isCorrect())
                .map(AnswerOption::getId)
                .findFirst().orElseThrow();
        answers.put(q1.getId(), List.of(wrongOption));

        QuizSubmission result = quizService.take(quiz.getId(), student.getId(), answers);

        assertThat(result.getScore()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple-choice: partial selection is not counted")
    void take_partialMultipleChoice_notCounted() {
        Map<Long, List<Long>> answers = allCorrectAnswers();
        Long oneCorrect = quizService.getOptions(q2.getId()).stream()
                .filter(AnswerOption::isCorrect)
                .map(AnswerOption::getId)
                .findFirst().orElseThrow();
        answers.put(q2.getId(), List.of(oneCorrect));

        QuizSubmission result = quizService.take(quiz.getId(), student.getId(), answers);

        assertThat(result.getScore()).isEqualTo(2);
    }

    @Test
    @DisplayName("Take quiz twice → DuplicateResourceException")
    void take_duplicate() {
        quizService.take(quiz.getId(), student.getId(), allCorrectAnswers());

        assertThatThrownBy(() ->
                quizService.take(quiz.getId(), student.getId(), allCorrectAnswers()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Module can have only one quiz")
    void createQuiz_duplicateOnModule() {
        Module module = quiz.getModule();

        assertThatThrownBy(() -> quizService.create(module.getId(), "Quiz 2", 10))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Question with SINGLE_CHOICE requires exactly one correct option")
    void addQuestion_singleChoice_twoCorrect() {
        Quiz q = quizService.create(
                createExtraModule().getId(),
                "Another quiz", 10);

        assertThatThrownBy(() -> quizService.addQuestion(q.getId(), "bad",
                QuestionType.SINGLE_CHOICE,
                List.of(new QuizService.OptionInput("a", true),
                        new QuizService.OptionInput("b", true))))
                .isInstanceOf(BusinessRuleException.class);
    }

    private Module createExtraModule() {
        User teacher = userService.createUser("T3", "t3-" + System.nanoTime() + "@ex.com", Role.TEACHER);
        var cat = categoryService.create("Cat3-" + System.nanoTime());
        Course course = courseService.create("C3", "d", "1w", LocalDate.now(),
                cat.getId(), teacher.getId());
        return courseService.addModule(course.getId(), "M3", 1, null);
    }
}