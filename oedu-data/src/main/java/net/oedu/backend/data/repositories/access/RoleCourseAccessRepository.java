package net.oedu.backend.data.repositories.access;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.access.AccessType;
import net.oedu.backend.data.entities.access.RoleCourseAccess;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.user.UserRole;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleCourseAccessRepository extends AutoIdRepository<RoleCourseAccess> {

    List<RoleCourseAccess> findAllByUserRole(UserRole userRole);

    List<RoleCourseAccess> findAllByAccessType(AccessType accessType);

    List<RoleCourseAccess> findAllByCourse(Course course);

    Optional<RoleCourseAccess> findByCourseAndUserRole(Course course, UserRole userRole);

    @Transactional
    void deleteAllByCourse(Course course);
}
