package net.oedu.backend.endpoints;


import io.netty.handler.codec.http.HttpResponseStatus;
import net.oedu.backend.base.endpoints.*;
import net.oedu.backend.base.server.ServerUtils;
import net.oedu.backend.data.entities.access.*;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.material.Material;
import net.oedu.backend.data.entities.user.User;
import net.oedu.backend.data.entities.user.UserSession;
import net.oedu.backend.data.repositories.access.RoleCourseAccessRepository;
import net.oedu.backend.data.repositories.access.RoleMaterialAccessRepository;
import net.oedu.backend.data.repositories.access.UserCourseAccessRepository;
import net.oedu.backend.data.repositories.access.UserMaterialAccessRepository;
import net.oedu.backend.data.repositories.course.CourseRepository;
import net.oedu.backend.data.repositories.material.MaterialRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FileEndpoint extends EndpointClass {
    public FileEndpoint() {
        super("file", "download / upload actions");
    }

    private MaterialRepository materialRepository;
    private CourseRepository courseRepository;
    private UserCourseAccessRepository userCourseAccessRepository;
    private RoleCourseAccessRepository roleCourseAccessRepository;
    private UserMaterialAccessRepository userMaterialAccessRepository;
    private RoleMaterialAccessRepository roleMaterialAccessRepository;

    @EndpointSetup
    public void setup(@EndpointParameter(value = "material", type = EndpointParameterType.REPOSITORY) final MaterialRepository materialRepository,
                      @EndpointParameter(value = "course", type = EndpointParameterType.REPOSITORY) final CourseRepository courseRepository,
                      @EndpointParameter(value = "userCourseAccess", type = EndpointParameterType.REPOSITORY) final UserCourseAccessRepository userCourseAccessRepository,
                      @EndpointParameter(value = "roleCourseAccess", type = EndpointParameterType.REPOSITORY) final RoleCourseAccessRepository roleCourseAccessRepository,
                      @EndpointParameter(value = "userMaterialAccess", type = EndpointParameterType.REPOSITORY) final UserMaterialAccessRepository userMaterialAccessRepository,
                      @EndpointParameter(value = "roleMaterialAccess", type = EndpointParameterType.REPOSITORY) final RoleMaterialAccessRepository roleMaterialAccessRepository) {
        this.materialRepository = materialRepository;
        this.courseRepository = courseRepository;
        this.userCourseAccessRepository = userCourseAccessRepository;
        this.roleCourseAccessRepository = roleCourseAccessRepository;
        this.userMaterialAccessRepository = userMaterialAccessRepository;
        this.roleMaterialAccessRepository = roleMaterialAccessRepository;
    }

    @Endpoint("upload_start")
    public Response startUpload(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                                @EndpointParameter(value = "course_uuid", optional = true) final UUID courseUuid,
                                @EndpointParameter("name") final String name,
                                @EndpointParameter("file_end") final String fileEnd) {
        Course course = null;
        if (courseUuid == null) {
             if (!user.isServerAdministrator())
                return new Response(HttpResponseStatus.FORBIDDEN, "NO_ACCESS_IN_ROOT");
        } else {
            course = courseRepository.findById(courseUuid).orElse(null);
            if (course == null) {
                return new Response(HttpResponseStatus.BAD_REQUEST, "NO_SUCH_COURSE");
            }
            if (roleCourseAccessRepository.findByCourseAndUserRole(course, user.getUserRole()).isEmpty()
                    && userCourseAccessRepository.findByCourseAndUser(course, user).isEmpty()
                    && !user.isServerAdministrator()) {
                return new Response(0, "NO_COURSE_ACCESS");
            }
        }


        final Material material = Material.create(materialRepository, name, fileEnd, course, user);
        return new Response(200, material).setAction(ResponseAction.UPLOAD_START, material);
    }

    @Endpoint("upload_end")
    public Response endUpload(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                              @EndpointParameter("material_uuid") final UUID materialUuid) {

        Optional<Material> materialOptional = materialRepository.findById(materialUuid);
        if (materialOptional.isEmpty()) {
            return new Response(0, "NO_SUCH_MATERIAL");
        }
        Material material = materialOptional.get();
        System.out.println(user.serializeJson());
        System.out.println(material.getCreator().serializeJson());
        if (!material.getCreator().getUuid().equals(user.getUuid())) {
            return new Response(0, "ACCESS_DENIED");
        }
        return new Response(200, material).setAction(ResponseAction.UPLOAD_END, material);
    }

    @Endpoint("delete")
    public Response delete(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                           @EndpointParameter("material_uuid") final UUID materialUuid) {
        Optional<Material> mat = materialRepository.findById(materialUuid);

        if (mat.isEmpty()) {
            return new Response(HttpResponseStatus.BAD_REQUEST, "NOW_SUCH_FILE");
        }
        Material material = mat.get();
        Optional<UserMaterialAccess> userAccess = userMaterialAccessRepository.findByUserAndMaterial(user, material);
        Optional<RoleMaterialAccess> roleAccess = roleMaterialAccessRepository.findByUserRoleAndMaterial(user.getUserRole(), material);

        List<AccessType> types = new ArrayList<>();
        userAccess.ifPresent(userMaterialAccess -> types.add(userMaterialAccess.getAccessType()));
        roleAccess.ifPresent(roleMaterialAccess -> types.add(roleMaterialAccess.getAccessType()));

        if (!types.contains(AccessType.ADMIN) && !material.getCreator().equals(user) && !user.isServerAdministrator()) {
            return new Response(HttpResponseStatus.UNAUTHORIZED, "ACCESS_DENIED");
        }

        File f = new File(String.valueOf(material.getUuid()));
        f.delete();
        userMaterialAccessRepository.deleteAllByMaterial(material);
        roleMaterialAccessRepository.deleteAllByMaterial(material);
        materialRepository.delete(material);
        return new Response(HttpResponseStatus.OK);
    }

    @Endpoint("delete_all")
    public Response deleteAll(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user) {
        if (user.isServerAdministrator()) {
            for (Material m : materialRepository.findAll()) {
                delete(user, m.getUuid());
            }
            return new Response(HttpResponseStatus.OK);
        } else {
            return new Response(HttpResponseStatus.FORBIDDEN);
        }
    }

    @Endpoint("get")
    public Response getMaterial(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                                @EndpointParameter(value = "session", type = EndpointParameterType.SESSION) final UserSession session,
                                @EndpointParameter("material_uuid") final UUID materialUuid) {
        Optional<Material> mat = materialRepository.findById(materialUuid);
        if (mat.isEmpty()) return new Response(HttpResponseStatus.BAD_REQUEST, "NO_SUCH_FILE");
        Material material = mat.get();
        Optional<UserMaterialAccess> userAccess = userMaterialAccessRepository.findByUserAndMaterial(user, material);
        Optional<RoleMaterialAccess> roleAccess = roleMaterialAccessRepository.findByUserRoleAndMaterial(user.getUserRole(), material);
        AtomicBoolean hasReadAccess = new AtomicBoolean(false);
        userAccess.ifPresent(access -> hasReadAccess.set(true));
        roleAccess.ifPresent(access -> hasReadAccess.set(true));
        if (user.isServerAdministrator() || material.getCreator().equals(user)) hasReadAccess.set(true);

        if (!hasReadAccess.get()) {
            return new Response(HttpResponseStatus.UNAUTHORIZED, "ACCESS_DENIED");
        }

        ServerUtils.sendFile(session, new File(String.valueOf(material.getUuid())));
        return new Response(HttpResponseStatus.OK, material);
    }

    @Endpoint("list")
    public Response listFiles(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                              @EndpointParameter(value = "parent_course_uuid", optional = true) final UUID parentCourseUuid) {
        if (parentCourseUuid == null) {
            return new Response(200, materialRepository.findMaterialsByCourse(null));
        } else {
            Course course = courseRepository.findById(parentCourseUuid).orElse(null);
            if (course == null) {
                return new Response(400, "UNKNOWN_COURSE");
            }
            UserCourseAccess userCourseAccess = userCourseAccessRepository.findByCourseAndUser(course, user).orElse(null);
            RoleCourseAccess roleCourseAccess = roleCourseAccessRepository.findByCourseAndUserRole(course, user.getUserRole()).orElse(null);
            if (!course.hasAccess(user, roleCourseAccess, userCourseAccess, AccessType.READ)) {
                return new Response(400, "ACCESS_DENIED");
            }

            return new Response(200, materialRepository.findMaterialsByCourse(course));
        }
    }
}
