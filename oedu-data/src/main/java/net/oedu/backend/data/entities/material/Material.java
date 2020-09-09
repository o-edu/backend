package net.oedu.backend.data.entities.material;

import com.google.gson.JsonObject;
import lombok.Data;
import net.oedu.backend.base.json.JsonBuilder;
import net.oedu.backend.base.json.JsonSerializable;
import net.oedu.backend.base.sql.models.TableModelAutoId;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.user.User;
import net.oedu.backend.data.repositories.material.MaterialRepository;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.OffsetDateTime;

@Data
@Entity
public final class Material extends TableModelAutoId implements JsonSerializable {
    private String name;
    @ManyToOne
    private Course course;
    private String fileEnd;
    private OffsetDateTime creation;
    @ManyToOne
    private User creator;

    public static Material create(final MaterialRepository rep, final String name, final String fileEnd, final Course course, final User creator) {
        Material m = new Material();
        m.name = name;
        m.creation = OffsetDateTime.now();
        m.fileEnd = fileEnd;
        m.course = course;
        m.creator = creator;
        rep.saveAndFlush(m);
        return m;
    }

    @Override
    public JsonObject serializeJson() {
        return JsonBuilder.create("name", name + fileEnd)
                .add("material_uuid", getUuid())
                .add("course", course)
                .build();
    }
}
