package net.oedu.backend.data.repositories.access;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.access.AccessType;
import net.oedu.backend.data.entities.access.UserCourseAccess;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.user.User;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserCourseAccessRepository extends AutoIdRepository<UserCourseAccess> {

    List<UserCourseAccess> findAllByUser(User user);

    List<UserCourseAccess> findAllByAccessType(AccessType accessType);

    List<UserCourseAccess> findAllByCourse(Course course);

    Optional<UserCourseAccess> findByCourseAndUser(Course course, User user);

    @Transactional
    void deleteAllByCourse(Course course);

    @Transactional
    void deleteAllByUser(User user);
}
