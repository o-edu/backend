package net.oedu.backend.endpoints;

import net.oedu.backend.base.endpoints.*;
import net.oedu.backend.base.server.ServerUtils;
import net.oedu.backend.data.entities.user.UserSession;

import java.util.List;
import java.util.UUID;

public final class TestEndpoints extends EndpointClass {
    public TestEndpoints() {
        super("test", "endpoints to test are here");
    }

    @Endpoint("notification")
    public Response testNotification(@EndpointParameter(value = "user_session", type = EndpointParameterType.SESSION) final UserSession userSession) {
        ServerUtils.sendMessage("session", userSession, new Response(200, "THE_NOTIFICATION"), "test");
        return new Response(200, "THE_RESPONSE");
    }

    @Endpoint("test_uuid_list")
    public Response testUuidList(@EndpointParameter("uuid_list") final List<UUID> uuidList) {
        return new Response(200, uuidList);
    }

    @Endpoint("test_list")
    public Response testList(@EndpointParameter("list") final List<String> list) {
        return new Response(200, list);
    }
}
