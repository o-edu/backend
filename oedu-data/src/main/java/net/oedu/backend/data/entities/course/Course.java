package net.oedu.backend.data.entities.course;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.oedu.backend.base.json.JsonBuilder;
import net.oedu.backend.base.json.JsonSerializable;
import net.oedu.backend.base.sql.models.TableModelAutoId;
import net.oedu.backend.data.entities.user.User;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Entity
public final class Course extends TableModelAutoId implements JsonSerializable {


    private String name;
    @ManyToOne
    private User creator;
    @ManyToOne
    private Course parentCourse;

    @Override
    public JsonObject serializeJson() {
        return JsonBuilder.create("name", name)
                .add("creator", creator)
                .add("parent_course", parentCourse == null ? null : parentCourse.getUuid())
                .add("uuid", getUuid())
                .build();
    }
}
