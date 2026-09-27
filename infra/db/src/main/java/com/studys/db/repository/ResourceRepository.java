package com.studys.db.repository;

import com.studys.db.entity.ResourceEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<ResourceEntity, UUID> {
    List<ResourceEntity> findByPlanNodeId(UUID planNodeId);
}
