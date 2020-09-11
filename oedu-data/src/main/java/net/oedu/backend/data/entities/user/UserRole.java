package net.oedu.backend.data.entities.user;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.oedu.backend.base.json.JsonBuilder;
import net.oedu.backend.base.json.JsonSerializable;
import net.oedu.backend.base.sql.models.TableModelAutoId;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@Entity
public final class UserRole extends TableModelAutoId implements JsonSerializable {

    @Column(unique = true)
    private String name;

    @Override
    public JsonObject serializeJson() {
        return JsonBuilder.create("name", name)
                .add("uuid", getUuid())
                .build();
    }
}
