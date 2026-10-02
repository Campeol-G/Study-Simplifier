package com.studys.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "content_cache")
public class ContentCacheEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "topico_slug", nullable = false, unique = true, length = 255)
    private String topicoSlug;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "conteudo", nullable = false)
    private String conteudo;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "fontes")
    private String fontes;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected ContentCacheEntity() {
    }

    public ContentCacheEntity(String topicoSlug, String tipo, String conteudo, String fontes) {
        this.topicoSlug = topicoSlug;
        this.tipo = tipo;
        this.conteudo = conteudo;
        this.fontes = fontes;
        this.atualizadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTopicoSlug() {
        return topicoSlug;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
        this.atualizadoEm = Instant.now();
    }

    public String getFontes() {
        return fontes;
    }

    public void setFontes(String fontes) {
        this.fontes = fontes;
        this.atualizadoEm = Instant.now();
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
