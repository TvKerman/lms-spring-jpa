package com.example.lms.service;

import com.example.lms.entity.Assignment;
import com.example.lms.entity.Lesson;
import com.example.lms.entity.Submission;
import com.example.lms.entity.User;
import com.example.lms.exception.BusinessRuleException;
import com.example.lms.exception.DuplicateResourceException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.repository.AssignmentRepository;
import com.example.lms.repository.LessonRepository;
import com.example.lms.repository.SubmissionRepository;
import com.example.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    public Assignment create(Long lessonId, String title, String description,
                             LocalDate dueDate, Integer maxScore) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));
        Assignment assignment = new Assignment();
        assignment.setLesson(lesson);
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setDueDate(dueDate);
        assignment.setMaxScore(maxScore);
        Assignment saved = assignmentRepository.save(assignment);
        log.info("Created assignment id={} for lesson id={}", saved.getId(), lessonId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Assignment findById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", id));
    }

    @Transactional(readOnly = true)
    public List<Assignment> findByLesson(Long lessonId) {
        return assignmentRepository.findByLessonId(lessonId);
    }

    public Submission submit(Long assignmentId, Long studentId, String content) {
        Assignment assignment = findById(assignmentId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", studentId));

        if (submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, studentId)) {
            throw new DuplicateResourceException(
                    "Student already submitted a solution for assignment id=" + assignmentId);
        }
        if (assignment.getDueDate() != null
                && LocalDate.now().isAfter(assignment.getDueDate())) {
            throw new BusinessRuleException(
                    "Deadline passed: " + assignment.getDueDate());
        }

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setContent(content);
        submission.setSubmittedAt(LocalDateTime.now());
        Submission saved = submissionRepository.save(submission);
        log.info("Submission id={} created: assignment={}, student={}",
                saved.getId(), assignmentId, studentId);
        return saved;
    }

    public Submission grade(Long submissionId, Integer score, String feedback) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", submissionId));
        Integer max = submission.getAssignment().getMaxScore();
        if (score < 0 || score > max) {
            throw new BusinessRuleException(
                    "Score must be between 0 and " + max + ", got " + score);
        }
        submission.setScore(score);
        submission.setFeedback(feedback);
        log.info("Graded submission id={}: score={}/{}", submissionId, score, max);
        return submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<Submission> findByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    @Transactional(readOnly = true)
    public List<Submission> findByStudent(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public Submission findByAssignmentAndStudent(Long assignmentId, Long studentId) {
        return submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Submission for assignment=" + assignmentId + ", student=" + studentId + " not found"));
    }
}