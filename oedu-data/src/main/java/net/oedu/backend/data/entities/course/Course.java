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
import net.oedu.backend.data.entities.user.UserRole;
import net.oedu.backend.data.entities.user.UserRoleBinding;
import net.oedu.backend.data.repositories.access.RoleCourseAccessRepository;
import net.oedu.backend.data.repositories.access.UserCourseAccessRepository;
import net.oedu.backend.data.repositories.user.UserRoleBindingRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public boolean hasAccess(final User user, final UserCourseAccess userCourseAccess, final AccessType minAccessType, final RoleCourseAccess... roleCourseAccesses) {
        if (user.isServerAdministrator() || creator.equals(user)) {
            return true;
        }
        boolean value = false;
        for (RoleCourseAccess roleCourseAccess : roleCourseAccesses) {
            if (!value && roleCourseAccess != null) value = roleCourseAccess.getAccessType().hasAccess(minAccessType);
        }
        if (!value && userCourseAccess != null) value = userCourseAccess.getAccessType().hasAccess(minAccessType);
        return value;
    }


    public static boolean hasAccess(final User user, final Course course, final AccessType accessType,
                                    final UserRoleBindingRepository userRoleBindingRepository,
                                    final RoleCourseAccessRepository roleCourseAccessRepository,
                                    final UserCourseAccessRepository userCourseAccessRepository) {
        if (course == null) {
            if (AccessType.READ.hasAccess(accessType)) {
                return true;
            } else {
                return user.isServerAdministrator();
            }
        }
        final List<UserRole> userRoles = userRoleBindingRepository.findAllByUser(user).stream().map(UserRoleBinding::getUserRole).collect(Collectors.toList());
        final List<RoleCourseAccess> roleCourseAccesses = new ArrayList<>();
        for (UserRole role : userRoles) {
            roleCourseAccesses.add(
                    roleCourseAccessRepository.findByCourseAndUserRole(course, role).orElse(null)
            );
        }
        final UserCourseAccess userCourseAccess = userCourseAccessRepository.findByCourseAndUser(course, user).orElse(null);
        return course.hasAccess(user, userCourseAccess, accessType, roleCourseAccesses.toArray(new RoleCourseAccess[0]));
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
