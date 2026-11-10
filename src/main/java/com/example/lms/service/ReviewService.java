package com.example.lms.service;

import com.example.lms.entity.Course;
import com.example.lms.entity.CourseReview;
import com.example.lms.entity.User;
import com.example.lms.exception.BusinessRuleException;
import com.example.lms.exception.DuplicateResourceException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.repository.CourseRepository;
import com.example.lms.repository.CourseReviewRepository;
import com.example.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewService {

    private final CourseReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public CourseReview leaveReview(Long studentId, Long courseId, Integer rating, String comment) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessRuleException("Rating must be between 1 and 5");
        }
        if (reviewRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new DuplicateResourceException("Student already left a review for this course");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", studentId));

        CourseReview review = new CourseReview();
        review.setCourse(course);
        review.setStudent(student);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now());
        CourseReview saved = reviewRepository.save(review);
        log.info("Review id={} left for course id={} by student id={}, rating={}",
                saved.getId(), courseId, studentId, rating);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<CourseReview> findByCourse(Long courseId) {
        return reviewRepository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public List<CourseReview> findByStudent(Long studentId) {
        return reviewRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(Long courseId) {
        return reviewRepository.findAverageRatingByCourseId(courseId);
    }

    public void delete(Long reviewId) {
        CourseReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("CourseReview", reviewId));
        reviewRepository.delete(review);
        log.info("Deleted review id={}", reviewId);
    }
}