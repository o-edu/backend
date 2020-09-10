package net.oedu.backend.data.entities.course;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.oedu.backend.base.json.JsonBuilder;
import net.oedu.backend.base.json.JsonSerializable;
import net.oedu.backend.base.sql.models.TableModelAutoId;
import net.oedu.backend.data.entities.access.AccessType;
import net.oedu.backend.data.entities.access.RoleCourseAccess;
import net.oedu.backend.data.entities.access.UserCourseAccess;
import net.oedu.backend.data.entities.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Entity
public final class Course extends TableModelAutoId implements JsonSerializable {

    public static final int MAX_NAME_LENGTH = 32;

    @Column(length = MAX_NAME_LENGTH)
    private String name;
    @ManyToOne
    private User creator;
    @ManyToOne
    private Course parentCourse;

    public boolean hasAccess(final User user, final RoleCourseAccess roleCourseAccess, final UserCourseAccess userCourseAccess, final AccessType minAccessType) {
        System.out.println(creator.equals(user));
        System.err.println(user.getName() + "-->" + creator.getName());
        if (user.isServerAdministrator() || creator.equals(user)) {
            return true;
        }
        boolean value;
        value = roleCourseAccess.getAccessType().hasAccess(minAccessType);
        if (!value) value = userCourseAccess.getAccessType().hasAccess(minAccessType);
        return value;
    }

    @Override
    public JsonObject serializeJson() {
        return JsonBuilder.create("name", name)
                .add("creator", creator)
                .add("parent_course", parentCourse == null ? null : parentCourse.getUuid())
                .add("uuid", getUuid())
                .build();
    }
}
