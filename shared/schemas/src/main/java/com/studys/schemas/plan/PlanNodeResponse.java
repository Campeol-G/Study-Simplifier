package com.studys.schemas.plan;

import java.util.UUID;

public class PlanNodeResponse {

    private UUID id;
    private String titulo;
    private String slug;
    private PlanNodeType tipo;
    private PlanNodeStatus status;
    private int ordem;
    private int nivelDominio;
    private UUID parentId;

    protected PlanNodeResponse() {
    }

    public PlanNodeResponse(UUID id, String titulo, String slug, PlanNodeType tipo, PlanNodeStatus status, int ordem, int nivelDominio, UUID parentId) {
        this.id = id;
        this.titulo = titulo;
        this.slug = slug;
        this.tipo = tipo;
        this.status = status;
        this.ordem = ordem;
        this.nivelDominio = nivelDominio;
        this.parentId = parentId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public PlanNodeType getTipo() {
        return tipo;
    }

    public void setTipo(PlanNodeType tipo) {
        this.tipo = tipo;
    }

    public PlanNodeStatus getStatus() {
        return status;
    }

    public void setStatus(PlanNodeStatus status) {
        this.status = status;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public int getNivelDominio() {
        return nivelDominio;
    }

    public void setNivelDominio(int nivelDominio) {
        this.nivelDominio = nivelDominio;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }
}
