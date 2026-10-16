package com.studys.llm;

import java.time.Duration;
import java.util.List;
import tools.jackson.databind.JsonNode;

public record Requisicao(String modelo, int maxTokens, Double temperatura, JsonNode esquema, List<Mensagem> mensagens, Duration timeout) {

    public Requisicao {
        mensagens = List.copyOf(mensagens);
    }
}
