package com.studys.schemas.content;

import java.util.UUID;

public class ResourceResponse {

    private UUID id;
    private ResourceType tipo;
    private String url;
    private String titulo;
    private String descricao;
    private String thumbnailUrl;
    private int ordem;

    protected ResourceResponse() {
    }

    public ResourceResponse(UUID id, ResourceType tipo, String url, String titulo, String descricao, String thumbnailUrl, int ordem) {
        this.id = id;
        this.tipo = tipo;
        this.url = url;
        this.titulo = titulo;
        this.descricao = descricao;
        this.thumbnailUrl = thumbnailUrl;
        this.ordem = ordem;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ResourceType getTipo() {
        return tipo;
    }

    public void setTipo(ResourceType tipo) {
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
}
