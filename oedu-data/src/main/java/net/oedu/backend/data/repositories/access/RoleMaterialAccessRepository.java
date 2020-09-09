package net.oedu.backend.data.repositories.access;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.access.AccessType;
import net.oedu.backend.data.entities.access.RoleMaterialAccess;
import net.oedu.backend.data.entities.material.Material;
import net.oedu.backend.data.entities.user.UserRole;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleMaterialAccessRepository extends AutoIdRepository<RoleMaterialAccess> {

    List<RoleMaterialAccess> findAllByUserRole(UserRole userRole);

    List<RoleMaterialAccess> findAllByAccessType(AccessType accessType);

    List<RoleMaterialAccess> findAllByMaterial(Material course);

    Optional<RoleMaterialAccess> findByUserRoleAndMaterial(UserRole userRole, Material material);

    void deleteAllByMaterial(Material material);
}
