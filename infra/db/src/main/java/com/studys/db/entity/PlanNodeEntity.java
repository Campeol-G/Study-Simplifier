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

@Entity
@Table(name = "plan_nodes")
public class PlanNodeEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanEntity plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private PlanNodeEntity parent;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "nivel_dominio", nullable = false)
    private int nivelDominio;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected PlanNodeEntity() {
    }

    public PlanNodeEntity(PlanEntity plan, PlanNodeEntity parent, String titulo, int nivelDominio, String status) {
        this.plan = plan;
        this.parent = parent;
        this.titulo = titulo;
        this.nivelDominio = nivelDominio;
        this.status = status;
        this.criadoEm = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public PlanEntity getPlan() {
        return plan;
    }

    public PlanNodeEntity getParent() {
        return parent;
    }

    public void setParent(PlanNodeEntity parent) {
        this.parent = parent;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public int getNivelDominio() {
        return nivelDominio;
    }

    public void setNivelDominio(int nivelDominio) {
        this.nivelDominio = nivelDominio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
