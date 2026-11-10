package com.example.lms.service;

import com.example.lms.entity.*;
import com.example.lms.entity.Module;
import com.example.lms.enums.QuestionType;
import com.example.lms.exception.BusinessRuleException;
import com.example.lms.exception.DuplicateResourceException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;

    public Quiz create(Long moduleId, String title, Integer timeLimit) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", moduleId));
        if (quizRepository.existsByModuleId(moduleId)) {
            throw new DuplicateResourceException("Module id=" + moduleId + " already has a quiz");
        }
        Quiz quiz = new Quiz();
        quiz.setModule(module);
        quiz.setTitle(title);
        quiz.setTimeLimit(timeLimit);
        Quiz saved = quizRepository.save(quiz);
        log.info("Created quiz id={} for module id={}", saved.getId(), moduleId);
        return saved;
    }

    public Question addQuestion(Long quizId, String text, QuestionType type,
                                List<OptionInput> options) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));
        if (options == null || options.size() < 2) {
            throw new BusinessRuleException("Question must have at least 2 options");
        }
        long correctCount = options.stream().filter(OptionInput::correct).count();
        if (correctCount == 0) {
            throw new BusinessRuleException("Question must have at least one correct option");
        }
        if (type == QuestionType.SINGLE_CHOICE && correctCount != 1) {
            throw new BusinessRuleException(
                    "SINGLE_CHOICE question must have exactly one correct option");
        }

        Question question = new Question();
        question.setQuiz(quiz);
        question.setText(text);
        question.setType(type);
        Question savedQuestion = questionRepository.save(question);
        quiz.getQuestions().add(savedQuestion);

        for (OptionInput input : options) {
            AnswerOption option = new AnswerOption();
            option.setQuestion(savedQuestion);
            option.setText(input.text());
            option.setCorrect(input.correct());
            AnswerOption savedOption = answerOptionRepository.save(option);
            savedQuestion.getOptions().add(savedOption);
        }
        log.info("Added question id={} to quiz id={}, options={}",
                savedQuestion.getId(), quizId, options.size());
        return savedQuestion;
    }

    public QuizSubmission take(Long quizId, Long studentId, Map<Long, List<Long>> answers) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", studentId));

        if (quizSubmissionRepository.existsByQuizIdAndStudentId(quizId, studentId)) {
            throw new DuplicateResourceException(
                    "Student already took this quiz (one attempt only)");
        }

        List<Question> questions = questionRepository.findByQuizId(quizId);
        if (questions.isEmpty()) {
            throw new BusinessRuleException("Quiz has no questions");
        }

        int score = 0;
        for (Question question : questions) {
            List<Long> selected = answers.getOrDefault(question.getId(), List.of());
            Set<Long> selectedSet = new HashSet<>(selected);

            Set<Long> correctSet = new HashSet<>();
            for (AnswerOption opt : answerOptionRepository.findByQuestionId(question.getId())) {
                if (opt.isCorrect()) correctSet.add(opt.getId());
            }

            if (selectedSet.equals(correctSet)) {
                score++;
            }
        }

        QuizSubmission submission = new QuizSubmission();
        submission.setQuiz(quiz);
        submission.setStudent(student);
        submission.setScore(score);
        submission.setTakenAt(LocalDateTime.now());
        QuizSubmission saved = quizSubmissionRepository.save(submission);
        log.info("Quiz id={} taken by student id={}: {}/{}",
                quizId, studentId, score, questions.size());
        return saved;
    }

    @Transactional(readOnly = true)
    public Quiz findById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", id));
    }

    @Transactional(readOnly = true)
    public Quiz findByModule(Long moduleId) {
        return quizRepository.findByModuleId(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz for module id=" + moduleId + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Question> getQuestions(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }

    @Transactional(readOnly = true)
    public List<AnswerOption> getOptions(Long questionId) {
        return answerOptionRepository.findByQuestionId(questionId);
    }

    @Transactional(readOnly = true)
    public List<QuizSubmission> getSubmissionsByStudent(Long studentId) {
        return quizSubmissionRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<QuizSubmission> getSubmissionsByQuiz(Long quizId) {
        return quizSubmissionRepository.findByQuizId(quizId);
    }

    public record OptionInput(String text, boolean correct) {}
}