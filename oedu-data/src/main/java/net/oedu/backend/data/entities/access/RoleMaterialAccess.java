package net.oedu.backend.data.entities.access;

import lombok.Data;
import net.oedu.backend.base.sql.models.TableModelAutoId;
import net.oedu.backend.data.entities.material.Material;
import net.oedu.backend.data.entities.user.UserRole;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Data
public class RoleMaterialAccess extends TableModelAutoId {

    private AccessType accessType;
    @ManyToOne
    private Material material;
    @ManyToOne
    private UserRole userRole;
}
