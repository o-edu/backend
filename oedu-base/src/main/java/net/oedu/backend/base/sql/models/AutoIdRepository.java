package net.oedu.backend.base.sql.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AutoIdRepository<T extends TableModelAutoId> extends JpaRepository<T, UUID> {

}
