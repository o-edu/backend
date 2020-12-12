package net.oedu.backend.endpoints.course;

import io.netty.handler.codec.http.HttpResponseStatus;
import net.oedu.backend.base.endpoints.*;
import net.oedu.backend.base.json.JsonBuilder;
import net.oedu.backend.data.entities.access.AccessType;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.user.User;
import net.oedu.backend.data.entities.user.UserRoleBinding;
import net.oedu.backend.data.repositories.access.RoleCourseAccessRepository;
import net.oedu.backend.data.repositories.access.UserCourseAccessRepository;
import net.oedu.backend.data.repositories.course.CourseRepository;
import net.oedu.backend.data.repositories.user.UserRoleBindingRepository;

import java.util.*;

public final class CourseEndpoints extends EndpointClass {

    public CourseEndpoints() {
        super("course", "course endpoints");
    }

    private CourseRepository courseRepository;
    private RoleCourseAccessRepository roleCourseAccessRepository;
    private UserCourseAccessRepository userCourseAccessRepository;
    private UserRoleBindingRepository userRoleBindingRepository;

    @EndpointSetup
    public void setup(@EndpointParameter(value = "course", type = EndpointParameterType.REPOSITORY) final CourseRepository courseRepository,
                      @EndpointParameter(value = "roleCourseAccess", type = EndpointParameterType.REPOSITORY) final RoleCourseAccessRepository roleCourseAccessRepository,
                      @EndpointParameter(value = "userCourseAccess", type = EndpointParameterType.REPOSITORY) final UserCourseAccessRepository userCourseAccessRepository,
                      @EndpointParameter(value = "userRoleBinding", type = EndpointParameterType.REPOSITORY) final UserRoleBindingRepository userRoleBindingRepository) {
        this.courseRepository = courseRepository;
        this.userCourseAccessRepository = userCourseAccessRepository;
        this.roleCourseAccessRepository = roleCourseAccessRepository;
        this.userRoleBindingRepository = userRoleBindingRepository;
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
        final Course parentCourse = courseRepository.findById(parentCourseUuid).get();

        if (this.hasAccess(user, parentCourse, AccessType.EDIT)) {
            return new Response(200, courseRepository.createCourse(parentCourse, name, user));
        }

        return new Response(400, "NO_RIGHTS_FOR_THAT_COURSE");
    }

    @Endpoint("has_access")
    public Response hasAccess(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                              @EndpointParameter("course_uuid") final UUID courseUuid,
                              @EndpointParameter(value = "type", optional = true) final String type) {
        final Course course = courseRepository.findById(courseUuid).orElse(null);
        final AccessType accessType = type == null ? AccessType.READ : AccessType.valueOf(type);
        return new Response(200, JsonBuilder.create("access", this.hasAccess(user, course, accessType)).build());
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

        if (this.hasAccess(user, course, AccessType.ADMIN)) {
            courseRepository.delete(course);
            return new Response(200);
        } else {
            return new Response(400, "ACCESS_DENIED");
        }
    }

    @Endpoint("list_courses")
    public Response listCourses(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                                @EndpointParameter(value = "parent_course_uuid", optional = true) final UUID parentCourseUuid) {
        final Set<Course> courseSet = new HashSet<>();

        if (parentCourseUuid == null) {
            if (this.hasAccess(user, null, AccessType.READ)) {
                courseSet.addAll(userCourseAccessRepository.findAllReadable(user));
                this.userRoleBindingRepository.findAllByUser(user).stream().map(UserRoleBinding::getUserRole).forEach(userRole -> {
                    courseSet.addAll(roleCourseAccessRepository.findAllReadable(userRole));
                });
            } else {
                return new Response(400, "NO_ACCESS");
            }

        } else {
            final Course parentCourse = courseRepository.findById(parentCourseUuid).orElse(null);
            if (parentCourse == null) {
                return new Response(400, "UNKNOWN_COURSE");
            }
            courseSet.addAll(courseRepository.findCoursesByParentCourse(parentCourse));
            courseSet.removeIf(course -> !this.hasAccess(user, course, AccessType.READ));
        }
        return new Response(200, courseSet);
    }

    private boolean hasAccess(final User user, final Course course, final AccessType accessType) {
        return Course.hasAccess(user, course, accessType, this.userRoleBindingRepository, this.roleCourseAccessRepository, this.userCourseAccessRepository);
    }
}
