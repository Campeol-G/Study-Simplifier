package com.studys.llm;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import tools.jackson.databind.JsonNode;

public record Requisicao(String modelo, int maxTokens, Double temperatura, JsonNode esquema, List<Mensagem> mensagens, Duration timeout) {

    public Requisicao {
        Objects.requireNonNull(mensagens, "mensagens é obrigatório");
        mensagens = List.copyOf(mensagens);
    }
}
