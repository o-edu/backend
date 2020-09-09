package net.oedu.backend.base.json;

import com.google.gson.JsonObject;

public final class JsonBuilder {

    private JsonObject element = new JsonObject();

    public static JsonBuilder create(final String key, final Object value) {
        JsonBuilder builder = new JsonBuilder();
        builder.add(key, value);
        return builder;
    }

    public JsonBuilder add(final String key, final Object value) {
        element.add(key, JsonUtils.toJson(value));
        return this;
    }

    public JsonObject build() {
        return element;
    }
}
