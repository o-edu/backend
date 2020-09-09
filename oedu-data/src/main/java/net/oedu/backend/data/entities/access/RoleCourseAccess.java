package net.oedu.backend.data.entities.access;

import lombok.Data;
import net.oedu.backend.base.sql.models.TableModelAutoId;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.user.UserRole;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Data
public class RoleCourseAccess extends TableModelAutoId {

    private AccessType accessType;
    @ManyToOne
    private Course course;
    @ManyToOne
    private UserRole userRole;
}
