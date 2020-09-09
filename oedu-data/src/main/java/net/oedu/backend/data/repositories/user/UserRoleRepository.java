package net.oedu.backend.data.repositories.user;

import net.oedu.backend.data.entities.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {

    UserRole findByName(String name);

    UserRole findByStatus(int status);
}
