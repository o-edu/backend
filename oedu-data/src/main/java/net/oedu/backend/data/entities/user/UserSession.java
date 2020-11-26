package net.oedu.backend.data.entities.user;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.oedu.backend.base.json.JsonBuilder;
import net.oedu.backend.base.json.JsonSerializable;
import net.oedu.backend.base.sql.models.TableModelAutoId;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
public final class UserSession extends TableModelAutoId implements JsonSerializable {

    @ManyToOne
    private User user;
    private OffsetDateTime creation;
    private OffsetDateTime lastUse;

    @Override
    public JsonObject serializeJson() {
        return JsonBuilder.create("token", getUuid())
                .add("user", user.serializeJson())
                .build();
    }
}
