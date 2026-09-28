package com.studys.schemas.content;

import java.time.Instant;

public class ContentResponse {

    private ContentType tipo;
    private String conteudo;
    private String fontes;
    private Instant atualizadoEm;

    protected ContentResponse() {
    }

    public ContentResponse(ContentType tipo, String conteudo, String fontes, Instant atualizadoEm) {
        this.tipo = tipo;
        this.conteudo = conteudo;
        this.fontes = fontes;
        this.atualizadoEm = atualizadoEm;
    }

    public ContentType getTipo() {
        return tipo;
    }

    public void setTipo(ContentType tipo) {
        this.tipo = tipo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getFontes() {
        return fontes;
    }

    public void setFontes(String fontes) {
        this.fontes = fontes;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
