package com.studys.schemas.plan;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PlanResponse {

    private UUID id;
    private String assunto;
    private String descricao;
    private PlanStatus status;
    private Instant criadoEm;
    private List<PlanNodeResponse> nodes;

    protected PlanResponse() {
    }

    public PlanResponse(UUID id, String assunto, String descricao, PlanStatus status, Instant criadoEm, List<PlanNodeResponse> nodes) {
        this.id = id;
        this.assunto = assunto;
        this.descricao = descricao;
        this.status = status;
        this.criadoEm = criadoEm;
        this.nodes = nodes;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public PlanStatus getStatus() {
        return status;
    }

    public void setStatus(PlanStatus status) {
        this.status = status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public List<PlanNodeResponse> getNodes() {
        return nodes;
    }

    public void setNodes(List<PlanNodeResponse> nodes) {
        this.nodes = nodes;
    }
}
