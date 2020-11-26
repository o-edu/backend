package net.oedu.backend.data.repositories.material;

import net.oedu.backend.base.sql.models.AutoIdRepository;
import net.oedu.backend.data.entities.course.Course;
import net.oedu.backend.data.entities.material.Material;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends AutoIdRepository<Material> {

    List<Material> findMaterialsByCourse(Course course);

    Optional<Material> findMaterialByCourseAndName(Course course, String name);
}
