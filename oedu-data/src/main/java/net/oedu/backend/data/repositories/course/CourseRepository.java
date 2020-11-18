package net.oedu.backend.data.repositories.course;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.user.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends AutoIdRepository<Course> {

    default Course createCourse(final Course parentCourse, final String name, final User creator) {
        Course course = new Course();
        course.setCreator(creator);
        course.setName(name);
        course.setParentCourse(parentCourse);
        return this.saveAndFlush(course);
    }

    List<Course> findCoursesByCreator(User creator);

    List<Course> findCoursesByParentCourse(Course parentCourse);

    Optional<Course> findCourseByParentCourseAndName(Course parentCourse, String name);
}
