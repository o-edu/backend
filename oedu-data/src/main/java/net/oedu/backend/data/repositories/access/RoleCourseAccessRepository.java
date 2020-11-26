package net.oedu.backend.data.repositories.access;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.access.AccessType;
import net.oedu.backend.data.entities.access.RoleCourseAccess;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.user.UserRole;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleCourseAccessRepository extends AutoIdRepository<RoleCourseAccess> {

    default RoleCourseAccess create(UserRole userRole, Course course, AccessType accessType) {
        RoleCourseAccess uca = new RoleCourseAccess();
        uca.setAccessType(accessType);
        uca.setCourse(course);
        uca.setUserRole(userRole);
        return this.saveAndFlush(uca);
    }

    @Query("select rca.course from RoleCourseAccess as rca where rca.userRole = ?1")
    List<Course> findAllReadable(UserRole user);

    List<RoleCourseAccess> findAllByUserRole(UserRole userRole);

    List<RoleCourseAccess> findAllByAccessType(AccessType accessType);

    List<RoleCourseAccess> findAllByCourse(Course course);

    Optional<RoleCourseAccess> findByCourseAndUserRole(Course course, UserRole userRole);

    @Transactional
    void deleteAllByCourse(Course course);
}
