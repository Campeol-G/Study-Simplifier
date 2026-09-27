package com.studys.db.repository;

import com.studys.db.entity.PlanNodeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanNodeRepository extends JpaRepository<PlanNodeEntity, UUID> {
    List<PlanNodeEntity> findByPlanId(UUID planId);
}
