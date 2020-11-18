package net.oedu.backend.data.repositories.access;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.access.AccessType;
import net.oedu.backend.data.entities.access.UserCourseAccess;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserCourseAccessRepository extends AutoIdRepository<UserCourseAccess> {

    default UserCourseAccess create(User user, Course course, AccessType accessType) {
        UserCourseAccess uca = new UserCourseAccess();
        uca.setAccessType(accessType);
        uca.setCourse(course);
        uca.setUser(user);
        return this.saveAndFlush(uca);
    }

    @Query("select uca.course from UserCourseAccess as uca where uca.user = ?1")
    List<Course> findAllReadable(User user);

    List<UserCourseAccess> findAllByUser(User user);

    List<UserCourseAccess> findAllByAccessType(AccessType accessType);

    List<UserCourseAccess> findAllByCourse(Course course);

    Optional<UserCourseAccess> findByCourseAndUser(Course course, User user);

    @Transactional
    void deleteAllByCourse(Course course);

    @Transactional
    void deleteAllByUser(User user);
}
