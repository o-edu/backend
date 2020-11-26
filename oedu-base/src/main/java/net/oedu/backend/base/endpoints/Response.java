package net.oedu.backend.base.endpoints;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.oedu.backend.base.json.JsonBuilder;
import net.oedu.backend.base.json.JsonSerializable;
import net.oedu.backend.base.json.JsonUtils;

@Getter
@RequiredArgsConstructor
public class Response {

    private final HttpResponseStatus status;
    private final JsonElement message;
    private ResponseAction responseAction = null;
    private Object data = null;

    public Response(final int status, final String message) {
        this(HttpResponseStatus.valueOf(status), JsonBuilder.create("error", JsonUtils.toJson(message)).build());
    }

    public Response(final int status) {
        this(HttpResponseStatus.valueOf(status), JsonObject::new);
    }

    public Response(final int status, final JsonSerializable message) {
        this(HttpResponseStatus.valueOf(status), message.serializeJson());
    }

    public Response(final int status, final JsonObject jsonObject) {
        this(HttpResponseStatus.valueOf(status), jsonObject);
    }

    public Response(final int status, final Object object) {
        this(HttpResponseStatus.valueOf(status), JsonUtils.toJson(object));
    }

    public Response(final HttpResponseStatus httpResponseStatus, final JsonSerializable message) {
        this(httpResponseStatus, message.serializeJson());
    }

    public Response(final HttpResponseStatus httpResponseStatus, final Object object) {
        this(httpResponseStatus, JsonUtils.toJson(object));
    }

    public Response(final HttpResponseStatus httpResponseStatus, final String message) {
        this(httpResponseStatus, JsonBuilder.create("error", message).build());
    }

    public Response(final HttpResponseStatus httpResponseStatus) {
        this(httpResponseStatus, JsonObject::new);
    }

    /**
     * sets the response action.
     *
     * @param responseAction the response action
     * @param data           data to give to the server
     * @return the object
     */
    public Response setAction(final ResponseAction responseAction, final Object data) {
        this.responseAction = responseAction;
        this.data = data;
        return this;
    }

    /**
     * @return the status as upper case string with underscores
     */
    public String statusAsString() {
        return this.status.toString().toUpperCase().replace(" ", "_");
    }
}
