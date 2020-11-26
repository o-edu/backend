package net.oedu.backend.data.repositories.user;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.user.User;
import net.oedu.backend.data.entities.user.UserRole;
import net.oedu.backend.data.entities.user.UserRoleBinding;

import java.util.List;

public interface UserRoleBindingRepository extends AutoIdRepository<UserRoleBinding> {

    default UserRoleBinding create(final User user, final UserRole userRole) {
        UserRoleBinding binding = new UserRoleBinding();
        binding.setUser(user);
        binding.setUserRole(userRole);

        return this.saveAndFlush(binding);
    }

    List<UserRoleBinding> findAllByUser(User user);

    List<UserRoleBinding> findAllByUserRole(UserRole userRole);
}
