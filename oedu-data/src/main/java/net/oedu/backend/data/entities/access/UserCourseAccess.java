package net.oedu.backend.data.entities.access;

import lombok.Getter;
import lombok.Setter;
import net.oedu.backend.base.sql.models.TableModelAutoId;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.user.User;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Entity
public class UserCourseAccess extends TableModelAutoId {

    private AccessType accessType;
    @ManyToOne
    private User user;
    @ManyToOne
    private Course course;


}
