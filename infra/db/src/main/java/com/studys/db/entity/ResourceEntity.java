package com.studys.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "resources")
public class ResourceEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_node_id", nullable = false)
    private PlanNodeEntity planNode;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "descricao")
    private String descricao;

    @Column(name = "thumbnail_url", length = 1024)
    private String thumbnailUrl;

    @Column(name = "ordem", nullable = false)
    private int ordem;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected ResourceEntity() {
    }

    public ResourceEntity(PlanNodeEntity planNode, String tipo, String url, String titulo, int ordem) {
        this.planNode = planNode;
        this.tipo = tipo;
        this.url = url;
        this.titulo = titulo;
        this.ordem = ordem;
        this.criadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public PlanNodeEntity getPlanNode() {
        return planNode;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
