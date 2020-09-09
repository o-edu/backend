package net.oedu.backend.server;

import lombok.Getter;
import net.oedu.backend.base.endpoints.EndpointClass;
import net.oedu.backend.base.endpoints.EndpointExecutor;
import net.oedu.backend.server.netty.NettyServer;
import org.reflections.Reflections;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Server {

    protected Server() {
        throw new RuntimeException("NOT_A_INSTANCE_CLASS");
    }

    @Getter
    private static EndpointExecutor endpointExecutor;
    @Getter
    private static AnnotationConfigApplicationContext applicationContext;

    public static void main(final String[] args) {


        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(SqlConfiguration.class);
        applicationContext.scan("net.oedu");
        applicationContext.refresh();

        endpointExecutor = new EndpointExecutor(applicationContext,
                new Reflections("net.oedu.backend.endpoints").getSubTypesOf(EndpointClass.class));

        try {
            new NettyServer(applicationContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
