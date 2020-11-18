package net.oedu.backend.base.sql.models;

import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@Getter
@MappedSuperclass
public class TableModelAutoId extends TableModel {

    @Id
    @Type(type = "uuid-char")
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", updatable = false, nullable = false)
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID uuid;

    /**
     * checks for the uuid.
     *
     * @param o the other object
     * @return boolean if the uuid is equal
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableModelAutoId that = (TableModelAutoId) o;

        return uuid.equals(that.uuid);
    }

    /**
     * @return hash code of the uuid
     */
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
