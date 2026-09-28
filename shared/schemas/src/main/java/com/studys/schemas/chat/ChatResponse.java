package com.studys.schemas.chat;

import java.util.List;
import java.util.UUID;

public class ChatResponse {

    private UUID conversationId;
    private ChatRole role;
    private String conteudo;
    private ChatStage estagio;
    private boolean onboardingConcluido;
    private List<String> sugestoes;
    private String assunto;
    private String nivel;

    protected ChatResponse() {
    }

    public ChatResponse(UUID conversationId, ChatRole role, String conteudo, ChatStage estagio, boolean onboardingConcluido, List<String> sugestoes, String assunto, String nivel) {
        this.conversationId = conversationId;
        this.role = role;
        this.conteudo = conteudo;
        this.estagio = estagio;
        this.onboardingConcluido = onboardingConcluido;
        this.sugestoes = sugestoes;
        this.assunto = assunto;
        this.nivel = nivel;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public ChatRole getRole() {
        return role;
    }

    public void setRole(ChatRole role) {
        this.role = role;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public ChatStage getEstagio() {
        return estagio;
    }

    public void setEstagio(ChatStage estagio) {
        this.estagio = estagio;
    }

    public boolean isOnboardingConcluido() {
        return onboardingConcluido;
    }

    public void setOnboardingConcluido(boolean onboardingConcluido) {
        this.onboardingConcluido = onboardingConcluido;
    }

    public List<String> getSugestoes() {
        return sugestoes;
    }

    public void setSugestoes(List<String> sugestoes) {
        this.sugestoes = sugestoes;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }
}
