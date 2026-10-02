package com.studys.db.repository;

import com.studys.db.entity.ContentCacheEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentCacheRepository extends JpaRepository<ContentCacheEntity, UUID> {
    Optional<ContentCacheEntity> findByTopicoSlugAndTipo(String topicoSlug, String tipo);
}
