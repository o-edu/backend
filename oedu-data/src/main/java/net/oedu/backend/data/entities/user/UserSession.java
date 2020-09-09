package net.oedu.backend.data.entities.user;

import com.google.gson.JsonObject;
import lombok.Data;
import net.oedu.backend.base.json.JsonBuilder;
import net.oedu.backend.base.json.JsonSerializable;
import net.oedu.backend.base.sql.models.TableModelAutoId;
import net.oedu.backend.data.repositories.user.UserSessionRepository;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.OffsetDateTime;

@Data
@Entity
public final class UserSession extends TableModelAutoId implements JsonSerializable {

    @ManyToOne
    private User user;
    private OffsetDateTime creation;
    private OffsetDateTime lastUse;

    public static UserSession create(final UserSessionRepository repository, final User user) {
        UserSession userSession = new UserSession();
        userSession.setUser(user);
        userSession.setCreation(OffsetDateTime.now());
        repository.save(userSession);
        return userSession;
    }

    @Override
    public JsonObject serializeJson() {
        return JsonBuilder.create("token", getUuid())
                .add("user", user.serializeJson())
                .build();
    }
}
