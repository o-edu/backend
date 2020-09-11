package net.oedu.backend.data.repositories.user;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.user.UserRole;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRoleRepository extends AutoIdRepository<UserRole> {

    UserRole findByName(String name);
}
