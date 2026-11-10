package com.example.lms.config;

import com.example.lms.entity.*;
import com.example.lms.entity.Module;
import com.example.lms.enums.QuestionType;
import com.example.lms.enums.Role;
import com.example.lms.repository.UserRepository;
import com.example.lms.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final UserService userService;
    private final CategoryService categoryService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final QuizService quizService;
    private final ReviewService reviewService;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains data, skipping seeding");
            return;
        }
        log.info("=== Seeding demo data ===");
        seed();
        log.info("=== Seeding complete ===");
    }

    private void seed() {
        Category programming = categoryService.create("Programming");
        Category databases  = categoryService.create("Databases");

        User teacher = userService.createUser("Ivan Petrov",  "ivan@example.com",  Role.TEACHER);
        User anna    = userService.createUser("Anna Ivanova", "anna@example.com",  Role.STUDENT);
        User petr    = userService.createUser("Petr Sidorov", "petr@example.com",  Role.STUDENT);

        userService.createOrUpdateProfile(teacher.getId(),
                "Hibernate instructor, 10+ years Java", null);
        userService.createOrUpdateProfile(anna.getId(),
                "Java enthusiast, learning ORM", null);

        Course hibernate = courseService.create(
                "Основы Hibernate",
                "Практический курс по ORM и Hibernate: маппинг, связи, ленивая загрузка",
                "8 weeks",
                LocalDate.now(),
                programming.getId(),
                teacher.getId()
        );

        Module m1 = courseService.addModule(hibernate.getId(),
                "Введение в ORM", 1, "Что такое ORM, JPA и Hibernate");
        Module m2 = courseService.addModule(hibernate.getId(),
                "Связи в JPA", 2, "OneToMany, ManyToOne, ManyToMany, OneToOne");

        Lesson l1 = courseService.addLesson(m1.getId(),
                "Зачем нужен ORM",
                "Теоретическое введение. Объектно-реляционное несоответствие.",
                "https://www.youtube.com/watch?v=example1");
        Lesson l2 = courseService.addLesson(m1.getId(),
                "Первый EntityManager",
                "Практика: настраиваем persistence.xml, создаём SessionFactory.",
                null);
        Lesson l3 = courseService.addLesson(m2.getId(),
                "OneToMany и ManyToOne",
                "Разбор двунаправленных связей, mappedBy и @JoinColumn.",
                null);

        Assignment a1 = assignmentService.create(l1.getId(),
                "Домашка 1: Настройка проекта",
                "Поднять проект с Hibernate, подключить PostgreSQL, создать первую сущность.",
                LocalDate.now().plusDays(7), 100);

        Assignment a2 = assignmentService.create(l3.getId(),
                "Домашка 2: Связи",
                "Смоделировать связь Course—Module—Lesson в своём проекте.",
                LocalDate.now().plusDays(14), 50);

        Quiz quiz = quizService.create(m1.getId(), "Тест по основам ORM", 15);

        quizService.addQuestion(quiz.getId(),
                "Что такое ORM?",
                QuestionType.SINGLE_CHOICE,
                List.of(
                        new QuizService.OptionInput("Object-Relational Mapping", true),
                        new QuizService.OptionInput("Object Random Method",      false),
                        new QuizService.OptionInput("Only Relational Memory",    false)
                ));

        quizService.addQuestion(quiz.getId(),
                "Какие аннотации относятся к JPA?",
                QuestionType.MULTIPLE_CHOICE,
                List.of(
                        new QuizService.OptionInput("@Entity",                 true),
                        new QuizService.OptionInput("@Table",                  true),
                        new QuizService.OptionInput("@SpringBootApplication", false)
                ));

        quizService.addQuestion(quiz.getId(),
                "По умолчанию @OneToMany загружается лениво?",
                QuestionType.SINGLE_CHOICE,
                List.of(
                        new QuizService.OptionInput("Да",  true),
                        new QuizService.OptionInput("Нет", false)
                ));

        courseService.addTag(hibernate.getId(), "Java");
        courseService.addTag(hibernate.getId(), "Hibernate");
        courseService.addTag(hibernate.getId(), "Beginner");

        enrollmentService.enroll(anna.getId(), hibernate.getId());
        enrollmentService.enroll(petr.getId(), hibernate.getId());

        Submission annaSubmission = assignmentService.submit(a1.getId(), anna.getId(),
                "Проект настроен, Hibernate подключён. Код в приложении.");
        assignmentService.grade(annaSubmission.getId(), 92, "Отличная работа, всё корректно.");

        Map<Long, List<Long>> answers = new HashMap<>();
        for (Question q : quizService.getQuestions(quiz.getId())) {
            List<Long> correctIds = quizService.getOptions(q.getId()).stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .toList();
            answers.put(q.getId(), correctIds);
        }
        QuizSubmission quizResult = quizService.take(quiz.getId(), anna.getId(), answers);

        reviewService.leaveReview(anna.getId(), hibernate.getId(),
                5, "Отличный курс, много практики и понятные объяснения.");

        log.info("Seeded: categories=2 (id={},{})", programming.getId(), databases.getId());
        log.info("Seeded: users=3 (teacher={}, students={},{})",
                teacher.getId(), anna.getId(), petr.getId());
        log.info("Seeded: course id={} '{}' with 2 modules, 3 lessons, 2 assignments",
                hibernate.getId(), hibernate.getTitle());
        log.info("Seeded: quiz id={} with 3 questions", quiz.getId());
        log.info("Seeded: enrollments=2, submission id={} graded, quizResult id={} score={}",
                annaSubmission.getId(), quizResult.getId(), quizResult.getScore());
    }
}