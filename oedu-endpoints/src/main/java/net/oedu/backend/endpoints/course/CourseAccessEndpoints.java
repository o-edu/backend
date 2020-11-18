package net.oedu.backend.endpoints.course;

import net.oedu.backend.base.endpoints.*;
import net.oedu.backend.data.entities.access.AccessType;
import net.oedu.backend.data.entities.access.RoleCourseAccess;
import net.oedu.backend.data.entities.access.UserCourseAccess;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.user.User;
import net.oedu.backend.data.entities.user.UserRole;
import net.oedu.backend.data.repositories.access.RoleCourseAccessRepository;
import net.oedu.backend.data.repositories.access.UserCourseAccessRepository;
import net.oedu.backend.data.repositories.course.CourseRepository;
import net.oedu.backend.data.repositories.user.UserRepository;
import net.oedu.backend.data.repositories.user.UserRoleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class CourseAccessEndpoints extends EndpointClass {
    public CourseAccessEndpoints() {
        super("course/access", "course access endpoints");
    }

    private CourseRepository courseRepository;
    private RoleCourseAccessRepository roleCourseAccessRepository;
    private UserCourseAccessRepository userCourseAccessRepository;
    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;

    @EndpointSetup
    public void setup(@EndpointParameter(value = "course", type = EndpointParameterType.REPOSITORY) final CourseRepository courseRepository,
                      @EndpointParameter(value = "roleCourseAccess", type = EndpointParameterType.REPOSITORY) final RoleCourseAccessRepository roleCourseAccessRepository,
                      @EndpointParameter(value = "userCourseAccess", type = EndpointParameterType.REPOSITORY) final UserCourseAccessRepository userCourseAccessRepository,
                      @EndpointParameter(value = "user", type = EndpointParameterType.REPOSITORY) final UserRepository userRepository,
                      @EndpointParameter(value = "userRole", type = EndpointParameterType.REPOSITORY) final UserRoleRepository userRoleRepository) {
        this.courseRepository = courseRepository;
        this.userCourseAccessRepository = userCourseAccessRepository;
        this.roleCourseAccessRepository = roleCourseAccessRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Endpoint("grant_user_access")
    public Response grantUserAccess(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                                    @EndpointParameter("course_uuid") final UUID courseUuid,
                                    @EndpointParameter("user_list") final List<UUID> userUuidList,
                                    @EndpointParameter("access_type") final String type) {
        Course course = courseRepository.findById(courseUuid).orElse(null);
        if (course == null) {
            return new Response(400, "NO_SUCH_COURSE");
        }
        UserCourseAccess userCourseAccess = userCourseAccessRepository.findByCourseAndUser(course, user).orElse(null);
        RoleCourseAccess roleCourseAccess = roleCourseAccessRepository.findByCourseAndUserRole(course, user.getUserRole()).orElse(null);
        if (!course.hasAccess(user, roleCourseAccess, userCourseAccess, AccessType.EDIT)) {
            return new Response(400, "ACCESS_DENIED");
        }
        AccessType accessType;
        try {
            accessType = AccessType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new Response(400, "UNKNOWN_ACCESS_TYPE");
        }

        if (!course.hasAccess(user, roleCourseAccess, userCourseAccess, AccessType.ADMIN)) {
            if (accessType.getLvl() >= AccessType.EDIT.getLvl()) {
                return new Response(400, "ACCESS_DENIED");
            }
        }

        List<User> userList = new ArrayList<>();
        for (UUID userUuid : userUuidList) {
            User u = userRepository.findById(userUuid).orElse(null);
            if (u == null) {
                return new Response(400, "ALL_USERS_MUST_EXIST (" + userUuid + ")");
            }
            userList.add(u);
        }

        for (User u : userList) {
            UserCourseAccess uca = userCourseAccessRepository.findByCourseAndUser(course, u).orElse(null);
            if (uca != null) {
                uca.setAccessType(accessType);
                userCourseAccessRepository.saveAndFlush(uca);
            } else {
                userCourseAccessRepository.create(u, course, accessType);
            }
        }

        return new Response(200);
    }

    @Endpoint("grant_role_access")
    public Response grantRoleAccess(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                                    @EndpointParameter("course_uuid") final UUID courseUuid,
                                    @EndpointParameter("role_uuid_list") final List<UUID> roleUuidList,
                                    @EndpointParameter("access_type") final String type) {
        Course course = courseRepository.findById(courseUuid).orElse(null);
        if (course == null) {
            return new Response(400, "UNKNOWN_COURSE");
        }

        UserCourseAccess userCourseAccess = userCourseAccessRepository.findByCourseAndUser(course, user).orElse(null);
        RoleCourseAccess roleCourseAccess = roleCourseAccessRepository.findByCourseAndUserRole(course, user.getUserRole()).orElse(null);
        if (!course.hasAccess(user, roleCourseAccess, userCourseAccess, AccessType.EDIT)) {
            return new Response(400, "ACCESS_DENIED");
        }
        AccessType accessType;
        try {
            accessType = AccessType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new Response(400, "UNKNOWN_ACCESS_TYPE");
        }

        if (!course.hasAccess(user, roleCourseAccess, userCourseAccess, AccessType.ADMIN)) {
            if (accessType.getLvl() >= AccessType.EDIT.getLvl()) {
                return new Response(400, "ACCESS_DENIED");
            }
        }


        List<UserRole> userRoleList = new ArrayList<>();
        for (UUID userRoleUuid : roleUuidList) {
            UserRole u = userRoleRepository.findById(userRoleUuid).orElse(null);
            if (u == null) {
                return new Response(400, "ALL_USER_ROLES_MUST_EXIST (" + userRoleUuid + ")");
            }
            userRoleList.add(u);
        }

        for (UserRole ur : userRoleList) {
            RoleCourseAccess rca = roleCourseAccessRepository.findByCourseAndUserRole(course, ur).orElse(null);
            if (rca != null) {
                rca.setAccessType(accessType);
                roleCourseAccessRepository.saveAndFlush(rca);
            } else {
                roleCourseAccessRepository.create(ur, course, accessType);
            }
        }
        return new Response(200);
    }


    @Endpoint("delete_role_access")
    public Response deleteRoleAccess(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                                     @EndpointParameter("course_uuid") final UUID courseUuid,
                                     @EndpointParameter("role_uuid_list") final List<UUID> roleUuidList) {
        Course course = courseRepository.findById(courseUuid).orElse(null);
        if (course == null) {
            return new Response(400, "UNKNOWN_COURSE");
        }

        UserCourseAccess userCourseAccess = userCourseAccessRepository.findByCourseAndUser(course, user).orElse(null);
        RoleCourseAccess roleCourseAccess = roleCourseAccessRepository.findByCourseAndUserRole(course, user.getUserRole()).orElse(null);
        boolean onlyEdit = false;
        if (!course.hasAccess(user, roleCourseAccess, userCourseAccess, AccessType.EDIT)) {
            onlyEdit = true;
        }


        List<RoleCourseAccess> roleCourseAccessList = new ArrayList<>();
        for (UUID userRoleUuid : roleUuidList) {
            UserRole u = userRoleRepository.findById(userRoleUuid).orElse(null);
            if (u == null) {
                return new Response(400, "ALL_USER_ROLES_MUST_EXIST (" + userRoleUuid + ")");
            }
            RoleCourseAccess rca = roleCourseAccessRepository.findByCourseAndUserRole(course, u).orElse(null);
            if (rca == null) {
                return new Response(400, "USER_ROLE_HAS_NO_ACCESS (" + userRoleUuid + ")");
            }
            if (rca.getAccessType().getLvl() >= AccessType.EDIT.getLvl() && onlyEdit) {
                return new Response(400, "ACCESS_DENIED_ONLY_NOT_EDITORS");
            }
            roleCourseAccessList.add(rca);
        }

        for (RoleCourseAccess rca : roleCourseAccessList) {
            roleCourseAccessRepository.delete(rca);
        }
        return new Response(200);
    }

    @Endpoint("delete_user_access")
    public Response deleteUserAccess(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                                     @EndpointParameter("course_uuid") final UUID courseUuid,
                                     @EndpointParameter("user_uuid_list") final List<UUID> userUuidList) {
        Course course = courseRepository.findById(courseUuid).orElse(null);
        if (course == null) {
            return new Response(400, "UNKNOWN_COURSE");
        }

        UserCourseAccess userCourseAccess = userCourseAccessRepository.findByCourseAndUser(course, user).orElse(null);
        RoleCourseAccess roleCourseAccess = roleCourseAccessRepository.findByCourseAndUserRole(course, user.getUserRole()).orElse(null);
        boolean onlyEdit = false;
        if (!course.hasAccess(user, roleCourseAccess, userCourseAccess, AccessType.EDIT)) {
            onlyEdit = true;
        }


        List<UserCourseAccess> userCourseAccessList = new ArrayList<>();
        for (UUID userUuid : userUuidList) {
            User u = userRepository.findById(userUuid).orElse(null);
            if (u == null) {
                return new Response(400, "ALL_USER_ROLES_MUST_EXIST (" + userUuid + ")");
            }
            UserCourseAccess uca = userCourseAccessRepository.findByCourseAndUser(course, u).orElse(null);
            if (uca == null) {
                return new Response(400, "USER_ROLE_HAS_NO_ACCESS (" + userUuid + ")");
            }
            if (uca.getAccessType().getLvl() >= AccessType.EDIT.getLvl() && onlyEdit) {
                return new Response(400, "ACCESS_DENIED_ONLY_NOT_EDITORS");
            }
            userCourseAccessList.add(uca);
        }

        for (UserCourseAccess uca : userCourseAccessList) {
            userCourseAccessRepository.delete(uca);
        }
        return new Response(200);
    }
}
