package net.oedu.backend.base.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import lombok.Getter;

import java.time.OffsetDateTime;

public final class JsonUtils {

    @Getter
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .serializeNulls()
            .serializeSpecialFloatingPointValues()
            .create();

    public static JsonElement toJson(final Object object) {
        if (object instanceof JsonElement) {
            return (JsonElement) object;
        }
        if (object instanceof OffsetDateTime) {
            return toJson(((OffsetDateTime) object).toEpochSecond());
        }
        return GSON.toJsonTree(object);
    }
}
