package net.oedu.backend.endpoints.course;

import io.netty.handler.codec.http.HttpResponseStatus;
import net.oedu.backend.base.endpoints.*;
import net.oedu.backend.base.json.JsonBuilder;
import net.oedu.backend.data.entities.access.AccessType;
import net.oedu.backend.data.entities.access.RoleCourseAccess;
import net.oedu.backend.data.entities.access.UserCourseAccess;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.user.User;
import net.oedu.backend.data.repositories.access.RoleCourseAccessRepository;
import net.oedu.backend.data.repositories.access.UserCourseAccessRepository;
import net.oedu.backend.data.repositories.course.CourseRepository;

import java.util.*;

public final class CourseEndpoints extends EndpointClass {

    public CourseEndpoints() {
        super("course", "course endpoints");
    }

    private CourseRepository courseRepository;
    private RoleCourseAccessRepository roleCourseAccessRepository;
    private UserCourseAccessRepository userCourseAccessRepository;

    @EndpointSetup
    public void setup(@EndpointParameter(value = "course", type = EndpointParameterType.REPOSITORY) final CourseRepository courseRepository,
                      @EndpointParameter(value = "roleCourseAccess", type = EndpointParameterType.REPOSITORY) final RoleCourseAccessRepository roleCourseAccessRepository,
                      @EndpointParameter(value = "userCourseAccess", type = EndpointParameterType.REPOSITORY) final UserCourseAccessRepository userCourseAccessRepository) {
        this.courseRepository = courseRepository;
        this.userCourseAccessRepository = userCourseAccessRepository;
        this.roleCourseAccessRepository = roleCourseAccessRepository;
    }

    @Endpoint("create")
    public Response createCourse(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                                 @EndpointParameter(value = "parent_course_uuid", optional = true) final UUID parentCourseUuid,
                                 @EndpointParameter("name") final String name) {

        if (name.length() > Course.MAX_NAME_LENGTH) {
            return new Response(HttpResponseStatus.BAD_REQUEST, "NAME_TOO_LONG");
        }
        if (parentCourseUuid != null && courseRepository.findById(parentCourseUuid).isEmpty()) {
            return new Response(HttpResponseStatus.BAD_REQUEST, "NO_SUCH_PARENT_COURSE");
        }
        if (parentCourseUuid == null) {
            if (user.isServerAdministrator()) {
                return new Response(200, courseRepository.createCourse(null, name, user));
            }
            return new Response(HttpResponseStatus.BAD_REQUEST, "NO_RIGHTS_HERE");
        }
        Course parentCourse = courseRepository.findById(parentCourseUuid).get();
        RoleCourseAccess roleCourseAccess = roleCourseAccessRepository.findByCourseAndUserRole(parentCourse, user.getUserRole()).orElse(null);
        UserCourseAccess userCourseAccess = userCourseAccessRepository.findByCourseAndUser(parentCourse, user).orElse(null);

        if (userCourseAccess != null && userCourseAccess.getAccessType().hasAccess(AccessType.EDIT)) {
            return new Response(200, courseRepository.createCourse(parentCourse, name, user));
        }

        if (roleCourseAccess != null && roleCourseAccess.getAccessType().hasAccess(AccessType.EDIT)) {
            return new Response(200, courseRepository.createCourse(parentCourse, name, user));
        }

        return new Response(200);
    }

    @Endpoint("has_access")
    public Response hasAccess(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                              @EndpointParameter("course_uuid") final UUID courseUuid,
                              @EndpointParameter(value = "type", optional = true) final String type) {
        final Course course = courseRepository.findById(courseUuid).orElse(null);
        AccessType accessType = type == null ? AccessType.READ : AccessType.valueOf(type);
        if (course == null) {
            return new Response(400, "NO_VALID_COURSE");
        }
        final RoleCourseAccess roleCourseAccess = roleCourseAccessRepository.findByCourseAndUserRole(course, user.getUserRole()).orElse(null);
        final UserCourseAccess userCourseAccess = userCourseAccessRepository.findByCourseAndUser(course, user).orElse(null);
        return new Response(200, JsonBuilder.create("access", course.hasAccess(user, roleCourseAccess, userCourseAccess, accessType)).build());
    }

    @Endpoint("info")
    public Response courseInfo(@EndpointParameter("course_uuid") final UUID courseUuid) {
        Course course = courseRepository.findById(courseUuid).orElse(null);
        if (course == null) {
            return new Response(400, "NO_SUCH_COURSE");
        } else {
            return new Response(200, course);
        }
    }

    @Endpoint("delete")
    public Response deleteCourse(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                                 @EndpointParameter("course_uuid") final UUID courseUuid) {
        Course course = courseRepository.findById(courseUuid).orElse(null);
        if (course == null) {
            return new Response(400, "NO_SUCH_COURSE");
        }
        RoleCourseAccess roleCourseAccess = roleCourseAccessRepository.findByCourseAndUserRole(course, user.getUserRole()).orElse(null);
        UserCourseAccess userCourseAccess = userCourseAccessRepository.findByCourseAndUser(course, user).orElse(null);

        if (course.hasAccess(user, roleCourseAccess, userCourseAccess, AccessType.ADMIN)) {
            courseRepository.delete(course);
            return new Response(200);
        } else {
            return new Response(400, "ACCESS_DENIED");
        }
    }

    @Endpoint("list_courses")
    public Response listCourses(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                                @EndpointParameter(value = "parent_course_uuid", optional = true) final UUID parentCourseUuid) {
        Set<Course> courseSet = new HashSet<>();

        if (parentCourseUuid == null) {
            courseSet.addAll(userCourseAccessRepository.findAllReadable(user));
            courseSet.addAll(roleCourseAccessRepository.findAllReadable(user.getUserRole()));
        } else {
            Course parentCourse = courseRepository.findById(parentCourseUuid).orElse(null);
            if (parentCourse == null) {
                return new Response(400, "UNKNOWN_COURSE");
            }
            courseSet.addAll(courseRepository.findCoursesByParentCourse(parentCourse));
            courseSet.removeIf(course -> !course.hasAccess(user,
                    roleCourseAccessRepository.findByCourseAndUserRole(course, user.getUserRole()).orElse(null),
                    userCourseAccessRepository.findByCourseAndUser(course, user).orElse(null),
                    AccessType.READ));
        }
        return new Response(200, courseSet);
    }
}
