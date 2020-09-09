package net.oedu.backend.data.repositories.user;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.user.User;
import net.oedu.backend.data.entities.user.UserRole;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends AutoIdRepository<User> {

    User findUserByName(String name);

    User findUserByMail(String mail);

    List<User> findUsersByUserRole(UserRole userRole);

    List<User> findAllByServerAdministrator(boolean serverAdministrator);
}
