package net.oedu.backend.data.repositories.access;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.access.AccessType;
import net.oedu.backend.data.entities.access.UserMaterialAccess;
import net.oedu.backend.data.entities.material.Material;
import net.oedu.backend.data.entities.user.User;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserMaterialAccessRepository extends AutoIdRepository<UserMaterialAccess> {

    List<UserMaterialAccess> findAllByUser(User user);

    List<UserMaterialAccess> findAllByAccessType(AccessType accessType);

    List<UserMaterialAccess> findAllByMaterial(Material material);

    Optional<UserMaterialAccess> findByUserAndMaterial(User user, Material material);

    @Transactional
    void deleteAllByMaterial(Material material);

    @Transactional
    void deleteAllByUser(User user);
}
