package net.oedu.backend.base.json;

import com.google.gson.JsonObject;

public interface JsonSerializable {

    JsonObject serializeJson();
}
