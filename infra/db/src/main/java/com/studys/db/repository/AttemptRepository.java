package com.studys.db.repository;

import com.studys.db.entity.AttemptEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptRepository extends JpaRepository<AttemptEntity, UUID> {
    List<AttemptEntity> findByPlanNodeId(UUID planNodeId);

    List<AttemptEntity> findByUserId(UUID userId);
}
