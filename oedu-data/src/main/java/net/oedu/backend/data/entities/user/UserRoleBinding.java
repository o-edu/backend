package net.oedu.backend.data.entities.user;

import lombok.Getter;
import lombok.Setter;
import net.oedu.backend.base.sql.models.TableModelAutoId;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Setter
@Getter
@Table(name = "user_userrole")
public class UserRoleBinding extends TableModelAutoId {

    @ManyToOne
    private User user;

    @ManyToOne
    private UserRole userRole;
}
