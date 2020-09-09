package net.oedu.backend.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import net.oedu.backend.base.endpoints.Response;
import net.oedu.backend.base.endpoints.ResponseAction;
import net.oedu.backend.base.server.ServerUtils;
import net.oedu.backend.base.sql.models.TableModelAutoId;
import net.oedu.backend.data.entities.user.User;
import net.oedu.backend.data.entities.user.UserSession;
import net.oedu.backend.data.repositories.user.UserRepository;
import net.oedu.backend.data.repositories.user.UserSessionRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;


public class NettyServer {

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));

    private UserSessionRepository userSessionRepository;


    public NettyServer(AnnotationConfigApplicationContext applicationContext) throws Exception {
        // Configure SSL.

        this.userSessionRepository = applicationContext.getBean(UserSessionRepository.class);

        ServerUtils.addListener(new ServerUtils.Action() {
            @Override
            public void sendMessage(final String type, final TableModelAutoId user, final Response response, final String tag) {
                if (type.equalsIgnoreCase("user")) {
                    for (UserSession userSession : userSessionRepository.findUserSessionsByUser((User) user)) {
                        try {
                            WebSocketFrameHandler.sendMessage(userSession, response, tag);
                        } catch (NullPointerException ignore) { }
                    }
                }
                if (type.equalsIgnoreCase("session")) {
                    WebSocketFrameHandler.sendMessage((UserSession) user, response, tag);
                }
            }

            @Override
            public void action(final TableModelAutoId session, final ResponseAction action, final Object data) {
                WebSocketFrameHandler.action((UserSession) session, action, data);
            }

            @Override
            public void sendFile(final TableModelAutoId session, final File file) {
                WebSocketFrameHandler.sendFile((UserSession) session, file);
            }
        });
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new WebSocketServerInitializer(sslCtx));

            Channel ch = b.bind(PORT).sync().channel();
            System.out.println("Open your web browser and navigate to "
                    + (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
