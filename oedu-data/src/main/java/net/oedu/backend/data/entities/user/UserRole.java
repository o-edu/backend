package net.oedu.backend.data.entities.user;

import com.google.gson.JsonObject;
import lombok.Data;
import net.oedu.backend.base.json.JsonBuilder;
import net.oedu.backend.base.json.JsonSerializable;
import net.oedu.backend.base.sql.models.TableModel;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public final class UserRole extends TableModel implements JsonSerializable {

    @Id
    private int status;

    private String name;

    @Override
    public JsonObject serializeJson() {
        return JsonBuilder.create("name", name)
                .add("status", status)
                .build();
    }
}
