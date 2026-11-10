package com.example.lms.service;

import com.example.lms.entity.*;
import com.example.lms.entity.Module;
import com.example.lms.exception.DuplicateResourceException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.repository.*;
import com.example.lms.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CourseReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;

    public Course create(String title, String description, String duration,
                         LocalDate startDate, Long categoryId, Long teacherId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User", teacherId));

        Course course = new Course();
        course.setTitle(title);
        course.setDescription(description);
        course.setDuration(duration);
        course.setStartDate(startDate);
        course.setCategory(category);
        course.setTeacher(teacher);
        Course saved = courseRepository.save(course);
        log.info("Created course id={}, title='{}', teacherId={}, categoryId={}",
                saved.getId(), title, teacherId, categoryId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    @Transactional(readOnly = true)
    public Course findWithStructure(Long id) {
        return courseRepository.findWithModulesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    @Transactional(readOnly = true)
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Course> findByCategory(Long categoryId) {
        return courseRepository.findByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public List<Course> findByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    public Course update(Long id, String title, String description, String duration, LocalDate startDate) {
        Course course = findById(id);
        if (title != null) course.setTitle(title);
        if (description != null) course.setDescription(description);
        if (duration != null) course.setDuration(duration);
        if (startDate != null) course.setStartDate(startDate);
        log.info("Updated course id={}", id);
        return courseRepository.save(course);
    }

    public void delete(Long id) {
        Course course = findById(id);
        courseRepository.delete(course);
        log.info("Deleted course id={} (cascade to modules/lessons/assignments)", id);
    }

    public Module addModule(Long courseId, String title, Integer orderIndex, String description) {
        Course course = findById(courseId);
        Module module = new Module();
        module.setCourse(course);
        module.setTitle(title);
        module.setOrderIndex(orderIndex);
        module.setDescription(description);
        course.getModules().add(module);
        Module saved = moduleRepository.save(module);
        log.info("Added module id={} to course id={}", saved.getId(), courseId);
        return saved;
    }

    public Lesson addLesson(Long moduleId, String title, String content, String videoUrl) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module", moduleId));
        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(title);
        lesson.setContent(content);
        lesson.setVideoUrl(videoUrl);
        module.getLessons().add(lesson);
        Lesson saved = lessonRepository.save(lesson);
        log.info("Added lesson id={} to module id={}", saved.getId(), moduleId);
        return saved;
    }

    public Course addTag(Long courseId, String tagName) {
        Course course = findById(courseId);
        Tag tag = tagRepository.findByName(tagName).orElseGet(() -> {
            Tag newTag = new Tag();
            newTag.setName(tagName);
            return tagRepository.save(newTag);
        });
        if (!course.getTags().add(tag)) {
            throw new DuplicateResourceException("Course already has tag '" + tagName + "'");
        }
        log.info("Tagged course id={} with '{}'", courseId, tagName);
        return courseRepository.save(course);
    }

    public Course removeTag(Long courseId, String tagName) {
        Course course = findById(courseId);
        Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new ResourceNotFoundException("Tag '" + tagName + "' not found"));
        course.getTags().remove(tag);
        log.info("Removed tag '{}' from course id={}", tagName, courseId);
        return courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseDetail(Long id) {
        Course c = courseRepository.findWithModulesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        Double avg = reviewRepository.findAverageRatingByCourseId(id);
        long count = enrollmentRepository.countByCourseId(id);

        List<ModuleResponse> modules = c.getModules().stream()
                .map(m -> new ModuleResponse(
                        m.getId(), m.getTitle(), m.getOrderIndex(), m.getDescription(),
                        m.getLessons().stream()
                                .map(l -> new LessonResponse(l.getId(), l.getTitle(), l.getContent(), l.getVideoUrl()))
                                .toList(),
                        m.getQuiz() != null))
                .toList();

        return new CourseDetailResponse(
                c.getId(), c.getTitle(), c.getDescription(), c.getDuration(), c.getStartDate(),
                c.getCategory().getId(), c.getCategory().getName(),
                c.getTeacher().getId(), c.getTeacher().getName(),
                modules,
                c.getTags().stream().map(Tag::getName).toList(),
                avg, count
        );
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> listCourseSummaries(Long categoryId, Long teacherId) {
        List<Course> courses;
        if (categoryId != null)     courses = courseRepository.findByCategoryId(categoryId);
        else if (teacherId != null) courses = courseRepository.findByTeacherId(teacherId);
        else                        courses = courseRepository.findAll();

        return courses.stream().map(c -> new CourseResponse(
                c.getId(), c.getTitle(), c.getDescription(), c.getDuration(), c.getStartDate(),
                c.getCategory().getId(), c.getCategory().getName(),
                c.getTeacher().getId(), c.getTeacher().getName()
        )).toList();
    }
}