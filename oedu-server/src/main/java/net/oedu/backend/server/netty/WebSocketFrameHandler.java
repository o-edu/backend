package net.oedu.backend.server.netty;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.oedu.backend.base.endpoints.Response;
import net.oedu.backend.base.endpoints.ResponseAction;
import net.oedu.backend.base.json.JsonBuilder;
import net.oedu.backend.base.json.JsonUtils;
import net.oedu.backend.base.upload.UploadFile;
import net.oedu.backend.data.entities.material.Material;
import net.oedu.backend.data.entities.user.User;
import net.oedu.backend.data.entities.user.UserSession;
import net.oedu.backend.data.repositories.user.UserSessionRepository;
import net.oedu.backend.server.Server;
import net.oedu.backend.server.info.WebsocketInfo;

import java.io.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public final class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled
        if (frame instanceof TextWebSocketFrame) {
            onTextMessage(ctx, (TextWebSocketFrame) frame);
        } else if (frame instanceof BinaryWebSocketFrame) {
            onBinaryMessage(ctx, (BinaryWebSocketFrame) frame);
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            System.out.println(frame);
            throw new UnsupportedOperationException(message);
        }
    }

    private void onTextMessage(final ChannelHandlerContext ctx, final TextWebSocketFrame frame) {
        String message = frame.text();
        System.out.println("Text");
        System.out.println("angekommen" + message);
        Gson gson = JsonUtils.getGSON();
        Map<?, ?> request;
        try {
            request = gson.fromJson(message, Map.class);

        } catch (JsonSyntaxException e) {
            sendMessage(ctx.channel(), false, new Response(HttpResponseStatus.BAD_REQUEST, "CORRUPT_JSON_SYNTAX"), "error");
            return;
        }
        if (request.get("tag") == null) {
            sendMessage(ctx.channel(), false, new Response(HttpResponseStatus.BAD_REQUEST, "TAG_MISSING"), "error");
            return;
        }
        if (request.get("endpoint") == null) {
            sendMessage(ctx.channel(), JsonBuilder.create("tag", request.get("tag")).add("error", "ENDPOINT_MISSING").build());
            return;
        }
        try {

            UserSession userSession = WebsocketInfo.getUserSession(ctx.channel());

            User user = null;
            if (userSession != null) {
                user = userSession.getUser();
                userSession.setLastUse(OffsetDateTime.now());
                Server.getApplicationContext().getBean(UserSessionRepository.class).saveAndFlush(userSession);
            }

            Response response = Server.getEndpointExecutor().execute(request, userSession, user);

            response = action(ctx.channel(), response.getResponseAction(), response.getData(), response);

            if (response == null) {
                return;
            }
            sendMessage(ctx.channel(), JsonBuilder.create("tag", request.get("tag"))
                    .add("status", response.statusAsString())
                    .add("data", response.getMessage())
                    .build());
        } catch (Exception e) {
            sendMessage(ctx.channel(), false, new Response(HttpResponseStatus.INTERNAL_SERVER_ERROR), (String) request.get("tag"));
            e.printStackTrace();
        }
    }

    private  void onBinaryMessage(final ChannelHandlerContext ctx, final BinaryWebSocketFrame frame) {
        if (WebsocketInfo.FILE_UPLOAD_MAP.containsKey(ctx.channel())) {
            FileOutputStream fos = WebsocketInfo.FILE_UPLOAD_MAP.get(ctx.channel()).getFos();
            ByteBuf output = frame.content().retain();
            try {
                output.readBytes(fos, output.readableBytes());
                fos.flush();
            } catch (IOException e) {
                sendMessage(ctx.channel(), false, new Response(500), "error");
            }
        } else {
            sendMessage(ctx.channel(), false, new Response(HttpResponseStatus.UNAUTHORIZED), "error");
        }
    }

    public static void action(final UserSession session, final ResponseAction action, final Object data) {
        action(WebsocketInfo.USER_CHANNEL_MAP.get(session.getUuid()), action, data, new Response(0, ""));
    }

    public static Response action(final Channel channel, final ResponseAction action, final Object data, final Response response) {
        if (action == null) {
            return response;
        }
        switch (action) {
            case LOG_IN:
                WebsocketInfo.CHANNEL_USER_MAP.put(channel, ((UserSession) data).getUuid());
                WebsocketInfo.USER_CHANNEL_MAP.put(((UserSession) data).getUuid(), channel);
                WebsocketInfo.USER_SESSIONS_IN_USE.put(((UserSession) data).getUuid(), (UserSession) data);
                break;
            case LOG_OUT:
                WebsocketInfo.CHANNEL_USER_MAP.remove(channel);
                WebsocketInfo.USER_CHANNEL_MAP.remove(((UserSession) data).getUuid());
                WebsocketInfo.USER_SESSIONS_IN_USE.remove(((UserSession) data).getUuid());
                break;
            case LOG_OUT_ALL:
                System.out.println(((List<UserSession>) data).size());
                for (UserSession session : (List<UserSession>) data) {
                    Channel logoutChannel = WebsocketInfo.USER_CHANNEL_MAP.get(session.getUuid());
                    System.err.println(logoutChannel);
                    if (logoutChannel == null) continue;
                    sendMessage(logoutChannel, true, new Response(200, "LOGGED_OUT"), "user-info");
                    action(logoutChannel, ResponseAction.LOG_OUT, session, null);
                }
                break;
            case UPLOAD_BREAKUP:
                String fileName = WebsocketInfo.FILE_UPLOAD_MAP.get(channel).getUuid();

                // delete file in database
                Map<?, ?> delData = JsonUtils.getGSON().fromJson("{\"material_uuid\": \"" + fileName + "\"}", Map.class);
                UserSession userSession = WebsocketInfo.getUserSession(channel);
                User user = null;
                if (userSession != null) {
                    user = userSession.getUser();
                }
                Server.getEndpointExecutor().execute("file/delete", delData, userSession, user);
                // delete file
                File file = WebsocketInfo.FILE_UPLOAD_MAP.get(channel).getFile();
                action(channel, ResponseAction.UPLOAD_END, data, null);
                if (!file.delete()) {
                    return new Response(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Not closed upload couldn't be deleted");
                }
                break;
            case UPLOAD_START:
                if (WebsocketInfo.FILE_UPLOAD_MAP.containsKey(channel)) {
                    sendMessage(channel, true, new Response(HttpResponseStatus.OK, "NOT closed upload is going to be deleted"), "upload");
                    action(channel, ResponseAction.UPLOAD_BREAKUP, data, response);
                }
                try {
                    String name = String.valueOf(((Material) data).getUuid());
                    File f = new File(name);
                    WebsocketInfo.FILE_UPLOAD_MAP.put(channel, new UploadFile(name));
                    if (!f.createNewFile()) {
                        try {
                            action(channel, ResponseAction.UPLOAD_BREAKUP, data, null);
                        } catch (NullPointerException ignore) { }
                        throw new RuntimeException("File couldn't be created " + name);
                    }
                    WebsocketInfo.FILE_UPLOAD_MAP.get(channel).create(f);
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Response(500, "UPLOAD_START_ERROR");
                }
                break;
            case UPLOAD_END:
                try {
                    WebsocketInfo.FILE_UPLOAD_MAP.get(channel).getFos().flush();
                    WebsocketInfo.FILE_UPLOAD_MAP.get(channel).getFos().close();
                    WebsocketInfo.FILE_UPLOAD_MAP.remove(channel);
                } catch (IOException e) {
                    e.printStackTrace();
                    return new Response(500, "UPLOAD_END_ERROR");
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    return new Response(500, "UPLOAD_NOT_RUNNING");
                }
                break;
            default:
                break;
        }
        return response;
    }

    public static void sendMessage(final UserSession userSession, final boolean notification, final Response response, final String tag) {
        sendMessage(WebsocketInfo.USER_CHANNEL_MAP.get(userSession.getUuid()), notification, response, tag);
    }

    public static void sendMessage(final Channel channel, final boolean notification, final Response response, final String tag) {
        JsonObject message = JsonBuilder.create(notification ? "notification-tag" : "tag", tag)
                .add("status", response.statusAsString())
                .add("data", response.getMessage())
                .build();
        sendMessage(channel, message);
    }

    public static void sendMessage(final Channel channel, final JsonObject jsonObject) {
        channel.writeAndFlush(new TextWebSocketFrame(jsonObject.toString()));
    }

    public static void sendFile(final UserSession userSession, final File file) {
        sendFile(WebsocketInfo.USER_CHANNEL_MAP.get(userSession.getUuid()), file);
    }

    public static void sendFile(final Channel channel, final File file) {
        ByteBuf byteBuf = Unpooled.buffer();
        try {
            FileInputStream fis = new FileInputStream(file);
            byteBuf.writeBytes(fis, (int) file.length());
            channel.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        sendMessage(ctx.channel(), false, new Response(500), "error");
    }
}
