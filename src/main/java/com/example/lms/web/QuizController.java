package com.example.lms.web;

import com.example.lms.entity.*;
import com.example.lms.service.QuizService;
import com.example.lms.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/{id}")
    public QuizResponse get(@PathVariable Long id) {
        Quiz q = quizService.findById(id);
        long count = quizService.getQuestions(id).size();
        return new QuizResponse(q.getId(), q.getModule().getId(),
                q.getTitle(), q.getTimeLimit(), count);
    }

    @PostMapping("/modules/{moduleId}")
    public ResponseEntity<QuizResponse> create(@PathVariable Long moduleId,
                                               @Valid @RequestBody CreateQuizRequest req) {
        Quiz q = quizService.create(moduleId, req.title(), req.timeLimit());
        return ResponseEntity.created(URI.create("/api/quizzes/" + q.getId()))
                .body(new QuizResponse(q.getId(), moduleId, q.getTitle(), q.getTimeLimit(), 0));
    }

    @GetMapping("/{id}/questions")
    public List<QuestionTakeResponse> questions(@PathVariable Long id) {
        return quizService.getQuestions(id).stream()
                .map(q -> new QuestionTakeResponse(q.getId(), q.getText(), q.getType().name(),
                        quizService.getOptions(q.getId()).stream()
                                .map(o -> new OptionResponse(o.getId(), o.getText()))
                                .toList()))
                .toList();
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<Void> addQuestion(@PathVariable Long id,
                                            @Valid @RequestBody CreateQuestionRequest req) {
        List<QuizService.OptionInput> inputs = req.options().stream()
                .map(o -> new QuizService.OptionInput(o.text(), o.correct()))
                .toList();
        quizService.addQuestion(id, req.text(), req.type(), inputs);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/{id}/take")
    public QuizSubmissionResponse take(@PathVariable Long id,
                                       @RequestParam Long studentId,
                                       @Valid @RequestBody TakeQuizRequest req) {
        Map<Long, List<Long>> answers = new HashMap<>();
        req.answers().forEach(a -> answers.put(a.questionId(), a.selectedOptionIds()));
        QuizSubmission qs = quizService.take(id, studentId, answers);
        return new QuizSubmissionResponse(qs.getId(), id, qs.getQuiz().getTitle(),
                qs.getStudent().getId(), qs.getStudent().getName(),
                qs.getScore(), qs.getTakenAt());
    }

    @GetMapping("/{id}/submissions")
    public List<QuizSubmissionResponse> submissions(@PathVariable Long id) {
        return quizService.getSubmissionsByQuiz(id).stream()
                .map(qs -> new QuizSubmissionResponse(qs.getId(),
                        qs.getQuiz().getId(), qs.getQuiz().getTitle(),
                        qs.getStudent().getId(), qs.getStudent().getName(),
                        qs.getScore(), qs.getTakenAt()))
                .toList();
    }
}