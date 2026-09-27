package com.studys.db.repository;

import com.studys.db.entity.PlanEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<PlanEntity, UUID> {
    List<PlanEntity> findByUserId(UUID userId);

    List<PlanEntity> findByUserIdOrderByCriadoEmDesc(UUID userId);
}
