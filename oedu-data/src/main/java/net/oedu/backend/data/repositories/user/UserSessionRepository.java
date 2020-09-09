package net.oedu.backend.data.repositories.user;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.user.User;
import net.oedu.backend.data.entities.user.UserSession;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;


@Repository
public interface UserSessionRepository extends AutoIdRepository<UserSession> {

    List<UserSession> findUserSessionsByUser(User user);

    @Transactional
    void deleteAllByUser(User user);
}
