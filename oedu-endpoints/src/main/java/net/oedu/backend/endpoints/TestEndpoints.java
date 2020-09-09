package net.oedu.backend.endpoints;

import net.oedu.backend.base.endpoints.*;
import net.oedu.backend.base.server.ServerUtils;
import net.oedu.backend.data.entities.user.UserSession;

public final class TestEndpoints extends EndpointClass {
    public TestEndpoints() {
        super("test", "endpoints to test are here");
    }

    @Endpoint("notification")
    public Response testNotification(@EndpointParameter(value = "user_session", type = EndpointParameterType.SESSION) final UserSession userSession) {
        ServerUtils.sendMessage("session", userSession, new Response(200, "THE_NOTIFICATION"), "test");
        return new Response(200, "THE_RESPONSE");
    }
}
