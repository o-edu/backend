package net.oedu.backend.data.repositories.user;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.user.UserRole;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRoleRepository extends AutoIdRepository<UserRole> {

    Optional<UserRole> findByName(String name);
}
