package com.example.lms.web;

import com.example.lms.entity.Assignment;
import com.example.lms.entity.Submission;
import com.example.lms.service.AssignmentService;
import com.example.lms.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @GetMapping("/{id}")
    public AssignmentResponse get(@PathVariable Long id) {
        Assignment a = assignmentService.findById(id);
        return new AssignmentResponse(a.getId(), a.getLesson().getId(),
                a.getTitle(), a.getDescription(), a.getDueDate(), a.getMaxScore());
    }

    @PostMapping("/lessons/{lessonId}")
    public ResponseEntity<AssignmentResponse> create(@PathVariable Long lessonId,
                                                     @Valid @RequestBody CreateAssignmentRequest req) {
        Assignment a = assignmentService.create(lessonId, req.title(), req.description(),
                req.dueDate(), req.maxScore());
        return ResponseEntity.created(URI.create("/api/assignments/" + a.getId()))
                .body(new AssignmentResponse(a.getId(), lessonId, a.getTitle(),
                        a.getDescription(), a.getDueDate(), a.getMaxScore()));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<SubmissionResponse> submit(@PathVariable Long id,
                                                     @RequestParam Long studentId,
                                                     @Valid @RequestBody SubmitAssignmentRequest req) {
        Submission s = assignmentService.submit(id, studentId, req.content());
        return ResponseEntity.created(URI.create("/api/submissions/" + s.getId()))
                .body(toSubmission(s));
    }

    @GetMapping("/{id}/submissions")
    public List<SubmissionResponse> submissions(@PathVariable Long id) {
        return assignmentService.findByAssignment(id).stream()
                .map(this::toSubmission).toList();
    }

    @PutMapping("/submissions/{submissionId}/grade")
    public SubmissionResponse grade(@PathVariable Long submissionId,
                                    @Valid @RequestBody GradeSubmissionRequest req) {
        return toSubmission(assignmentService.grade(submissionId, req.score(), req.feedback()));
    }

    private SubmissionResponse toSubmission(Submission s) {
        return new SubmissionResponse(s.getId(),
                s.getAssignment().getId(),
                s.getStudent().getId(), s.getStudent().getName(),
                s.getSubmittedAt(), s.getContent(), s.getScore(), s.getFeedback());
    }
}