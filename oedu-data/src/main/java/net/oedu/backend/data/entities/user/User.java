package net.oedu.backend.data.entities.user;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.oedu.backend.base.json.JsonBuilder;
import net.oedu.backend.base.json.JsonSerializable;
import net.oedu.backend.base.security.Hashing;
import net.oedu.backend.base.sql.models.TableModelAutoId;
import net.oedu.backend.data.repositories.user.UserRepository;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.OffsetDateTime;

@Data
@Entity
@Getter
@Setter
public final class User extends TableModelAutoId implements JsonSerializable {

    @Column(unique = true)
    private String name;
    @Column(unique = true)
    private String mail;
    private String passwordHash;
    @Column(updatable = false)
    private OffsetDateTime creation;
    private OffsetDateTime lastLogin;
    private boolean serverAdministrator;

    @ManyToOne
    private UserRole userRole;

    public static User createUser(final UserRepository repository, final String name, final String mail, final String pw, final UserRole userRole) {
        User user = new User();
        user.setName(name);
        user.setMail(mail);
        user.setPasswordHash(Hashing.hash(pw));
        user.setCreation(OffsetDateTime.now());
        user.setLastLogin(OffsetDateTime.now());
        user.setUserRole(userRole);
        user.setServerAdministrator(false);
        repository.saveAndFlush(user);
        return user;
    }

    @Override
    public JsonObject serializeJson() {
        return JsonBuilder.create("name", name)
                .add("mail", mail)
                .add("creation", creation)
                .add("last_login", lastLogin)
                .add("user_role", userRole.serializeJson())
                .build();
    }
}
