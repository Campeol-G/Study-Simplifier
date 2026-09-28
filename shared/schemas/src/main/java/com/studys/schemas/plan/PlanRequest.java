package com.studys.schemas.plan;

public class PlanRequest {

    private String assunto;
    private String descricao;

    protected PlanRequest() {
    }

    public PlanRequest(String assunto, String descricao) {
        this.assunto = assunto;
        this.descricao = descricao;
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
}
