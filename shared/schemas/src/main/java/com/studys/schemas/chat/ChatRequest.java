package com.studys.schemas.chat;

import java.util.UUID;

public class ChatRequest {

    private UUID conversationId;
    private String conteudo;

    protected ChatRequest() {
    }

    public ChatRequest(UUID conversationId, String conteudo) {
        this.conversationId = conversationId;
        this.conteudo = conteudo;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
}
