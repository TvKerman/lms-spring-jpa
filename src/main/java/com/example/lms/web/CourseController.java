package com.example.lms.web;

import com.example.lms.entity.*;
import com.example.lms.entity.Module;
import com.example.lms.service.*;
import com.example.lms.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final ReviewService reviewService;

    @GetMapping
    public List<CourseResponse> list(@RequestParam(required = false) Long categoryId,
                                     @RequestParam(required = false) Long teacherId) {
        return courseService.listCourseSummaries(categoryId, teacherId);
    }

    @GetMapping("/{id}")
    public CourseDetailResponse detail(@PathVariable Long id) {
        return courseService.getCourseDetail(id);
    }

    @PostMapping
    public ResponseEntity<CourseDetailResponse> create(@Valid @RequestBody CreateCourseRequest req) {
        Course c = courseService.create(req.title(), req.description(), req.duration(),
                req.startDate(), req.categoryId(), req.teacherId());
        return ResponseEntity.created(URI.create("/api/courses/" + c.getId()))
                .body(courseService.getCourseDetail(c.getId()));
    }

    @PutMapping("/{id}")
    public CourseDetailResponse update(@PathVariable Long id,
                                       @Valid @RequestBody UpdateCourseRequest req) {
        courseService.update(id, req.title(), req.description(), req.duration(), req.startDate());
        return courseService.getCourseDetail(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/modules")
    public ResponseEntity<ModuleResponse> addModule(@PathVariable Long courseId,
                                                    @Valid @RequestBody CreateModuleRequest req) {
        Module m = courseService.addModule(courseId, req.title(), req.orderIndex(), req.description());
        return ResponseEntity.created(URI.create("/api/modules/" + m.getId()))
                .body(new ModuleResponse(m.getId(), m.getTitle(), m.getOrderIndex(),
                        m.getDescription(), List.of(), false));
    }

    @PostMapping("/{courseId}/tags")
    public ResponseEntity<Void> addTag(@PathVariable Long courseId,
                                       @RequestParam String name) {
        courseService.addTag(courseId, name);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{courseId}/tags/{name}")
    public ResponseEntity<Void> removeTag(@PathVariable Long courseId, @PathVariable String name) {
        courseService.removeTag(courseId, name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{courseId}/enrollments")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable Long courseId,
                                                     @Valid @RequestBody EnrollRequest req) {
        Enrollment e = enrollmentService.enroll(req.studentId(), courseId);
        return ResponseEntity.created(URI.create("/api/courses/" + courseId + "/enrollments/" + e.getId()))
                .body(new EnrollmentResponse(e.getId(),
                        e.getStudent().getId(), e.getStudent().getName(),
                        e.getCourse().getId(), e.getCourse().getTitle(),
                        e.getEnrollDate(), e.getStatus().name()));
    }

    @GetMapping("/{courseId}/enrollments")
    public List<EnrollmentResponse> listEnrollments(@PathVariable Long courseId) {
        return enrollmentService.getCourseEnrollments(courseId).stream()
                .map(e -> new EnrollmentResponse(e.getId(),
                        e.getStudent().getId(), e.getStudent().getName(),
                        e.getCourse().getId(), e.getCourse().getTitle(),
                        e.getEnrollDate(), e.getStatus().name()))
                .toList();
    }

    @DeleteMapping("/{courseId}/enrollments/{studentId}")
    public ResponseEntity<Void> unenroll(@PathVariable Long courseId,
                                         @PathVariable Long studentId) {
        enrollmentService.unenroll(studentId, courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{courseId}/reviews")
    public List<ReviewResponse> listReviews(@PathVariable Long courseId) {
        return reviewService.findByCourse(courseId).stream()
                .map(r -> new ReviewResponse(r.getId(),
                        r.getCourse().getId(),
                        r.getStudent().getId(), r.getStudent().getName(),
                        r.getRating(), r.getComment(), r.getCreatedAt()))
                .toList();
    }

    @PostMapping("/{courseId}/reviews")
    public ResponseEntity<ReviewResponse> addReview(@PathVariable Long courseId,
                                                    @RequestParam Long studentId,
                                                    @Valid @RequestBody CreateReviewRequest req) {
        CourseReview r = reviewService.leaveReview(studentId, courseId, req.rating(), req.comment());
        return ResponseEntity.created(URI.create("/api/courses/" + courseId + "/reviews/" + r.getId()))
                .body(new ReviewResponse(r.getId(),
                        r.getCourse().getId(),
                        r.getStudent().getId(), r.getStudent().getName(),
                        r.getRating(), r.getComment(), r.getCreatedAt()));
    }

    private CourseResponse toSummary(Course c) {
        return new CourseResponse(
                c.getId(), c.getTitle(), c.getDescription(), c.getDuration(), c.getStartDate(),
                c.getCategory().getId(), c.getCategory().getName(),
                c.getTeacher().getId(), c.getTeacher().getName()
        );
    }
}