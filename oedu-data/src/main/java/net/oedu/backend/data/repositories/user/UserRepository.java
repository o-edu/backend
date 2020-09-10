package net.oedu.backend.data.repositories.user;

import net.oedu.backend.base.security.Hashing;
import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.user.User;
import net.oedu.backend.data.entities.user.UserRole;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface UserRepository extends AutoIdRepository<User> {

    User findUserByName(String name);

    User findUserByMail(String mail);

    List<User> findUsersByUserRole(UserRole userRole);

    List<User> findAllByServerAdministrator(boolean serverAdministrator);

    default User createUser(final String name, final String mail, final String pw, final UserRole userRole) {
        User user = new User();
        user.setName(name);
        user.setMail(mail);
        user.setPasswordHash(Hashing.hash(pw));
        user.setCreation(OffsetDateTime.now());
        user.setLastLogin(OffsetDateTime.now());
        user.setUserRole(userRole);
        user.setServerAdministrator(false);
        this.saveAndFlush(user);
        return user;
    }
}
