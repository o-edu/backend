package net.oedu.backend.endpoints;

import net.oedu.backend.base.endpoints.*;
import net.oedu.backend.base.security.Hashing;
import net.oedu.backend.base.server.ServerUtils;
import net.oedu.backend.data.entities.user.UserRole;
import net.oedu.backend.data.entities.user.UserSession;
import net.oedu.backend.data.repositories.access.UserCourseAccessRepository;
import net.oedu.backend.data.repositories.access.UserMaterialAccessRepository;
import net.oedu.backend.data.repositories.user.UserRepository;
import net.oedu.backend.data.entities.user.User;
import net.oedu.backend.data.repositories.user.UserRoleRepository;
import net.oedu.backend.data.repositories.user.UserSessionRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public final class UserEndpoint extends EndpointClass {

    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;
    private UserSessionRepository userSessionRepository;

    private UserCourseAccessRepository userCourseAccessRepository;
    private UserMaterialAccessRepository userMaterialAccessRepository;

    public UserEndpoint() {
        super("user", "user actions");
    }

    @EndpointSetup
    public void setup(@EndpointParameter(value = "user", type = EndpointParameterType.REPOSITORY) final UserRepository userRepository,
                      @EndpointParameter(value = "userRole", type = EndpointParameterType.REPOSITORY) final UserRoleRepository userRoleRepository,
                      @EndpointParameter(value = "userSession", type = EndpointParameterType.REPOSITORY) final UserSessionRepository userSessionRepository,

                      @EndpointParameter(value = "userMaterialAccess", type = EndpointParameterType.REPOSITORY)
                          final UserMaterialAccessRepository userMaterialAccessRepository,

                      @EndpointParameter(value = "userCourseAccess", type = EndpointParameterType.REPOSITORY)
                          final UserCourseAccessRepository userCourseAccessRepository) {

        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.userSessionRepository = userSessionRepository;

        this.userCourseAccessRepository = userCourseAccessRepository;
        this.userMaterialAccessRepository = userMaterialAccessRepository;
    }

    @Endpoint("register")
    public Response register(@EndpointParameter(value = "user", type = EndpointParameterType.USER, optional = true) final User u,
                             @EndpointParameter("name") final String name,
                             @EndpointParameter("password") final String password,
                             @EndpointParameter("mail") final String mail) {

        if (u != null) {
            return new Response(400, "ALREADY_LOGGED_IN");
        }

        if (userRoleRepository.findByStatus(0) == null) {
            UserRole role = new UserRole();
            role.setStatus((short) 0);
            role.setName("default");
            userRoleRepository.save(role);
        }
        User user = User.createUser(userRepository, name, mail, password, userRoleRepository.findByStatus(0));
        UserSession userSession = UserSession.create(userSessionRepository, user);


        return new Response(200, UserSession.create(userSessionRepository, user))
                .setAction(ResponseAction.LOG_IN, userSession);
    }

    @Endpoint("login")
    public Response login(@EndpointParameter(value = "user", type = EndpointParameterType.USER, optional = true) final User u,
                          @EndpointParameter("name") final String name,
                          @EndpointParameter("password") final String password) {

        if (u != null) {
            return new Response(400, "ALREADY_LOGGED_IN");
        }

        Response response = new Response(400, "ACCESS_DENIED");
        try {
            User user = userRepository.findUserByName(name);
            if (Hashing.verify(password, user.getPasswordHash())) {
                user.setLastLogin(OffsetDateTime.now());
                userRepository.save(user);
                userSessionRepository.deleteAllByUser(user);
                UserSession userSession = UserSession.create(userSessionRepository, user);
                response = new Response(200, userSession)
                        .setAction(ResponseAction.LOG_IN, userSession);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @Endpoint("session")
    public Response sessionLogin(@EndpointParameter(value = "user", type = EndpointParameterType.USER, optional = true) final User u,
                                 @EndpointParameter("token") final String token) {

        if (u != null) {
            return new Response(400, "ALREADY_LOGGED_IN");
        }

        Optional<UserSession> session = userSessionRepository.findById(UUID.fromString(token));
        if (session.isEmpty()) {
            return new Response(400, "INVALID_SESSION");
        }
        User user = session.get().getUser();
        userSessionRepository.delete(session.get());
        UserSession userSession = UserSession.create(userSessionRepository, user);
        return new Response(200, userSession).setAction(ResponseAction.LOG_IN, userSession);
    }

    @Endpoint("logout")
    public Response logout(@EndpointParameter(value = "user", type = EndpointParameterType.SESSION) final UserSession session) {
        ServerUtils.action(session, ResponseAction.LOG_OUT, session);
        userSessionRepository.delete(session);
        return new Response(200);
    }

    @Endpoint("change_password")
    public Response changePassword(@EndpointParameter(value = "user", type = EndpointParameterType.USER) final User user,
                                   @EndpointParameter("old_password") final String oldPassword,
                                   @EndpointParameter("new_password") final String newPassword) {
        if (Hashing.verify(oldPassword, user.getPasswordHash())) {
            user.setPasswordHash(Hashing.hash(newPassword));
        } else {
            return new Response(400, "WRONG_PASSWORD");
        }
        return new Response(200);
    }

    @Endpoint("delete")
    public Response delete(@EndpointParameter(value = "session", type = EndpointParameterType.SESSION) final UserSession userSession) {
        Response logout = logout(userSession);
        User user = userSession.getUser();
        userSessionRepository.deleteAllByUser(user);
        userMaterialAccessRepository.deleteAllByUser(user);
        userCourseAccessRepository.deleteAllByUser(user);
        userRepository.delete(user);
        return new Response(200);
    }
}
