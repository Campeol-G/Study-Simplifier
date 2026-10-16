package com.studys.llm;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record RequisicaoHttp(
        MetodoHttp metodo, String url, Map<String, String> cabecalhos, String corpo, Duration timeout) {

    private static final String VALOR_MASCARADO = "***";

    public RequisicaoHttp {
        Objects.requireNonNull(metodo, "metodo é obrigatório");
        Objects.requireNonNull(url, "url é obrigatória");
        Objects.requireNonNull(timeout, "timeout é obrigatório");
        cabecalhos = cabecalhos == null ? Map.of() : Map.copyOf(cabecalhos);
        corpo = corpo == null ? "" : corpo;
    }

    /**
     * Esconde o valor dos cabeçalhos que carregam credencial ({@code x-api-key} e
     * {@code Authorization}, sem diferenciar maiúsculas de minúsculas no nome); todo o resto
     * da requisição continua visível para depuração.
     */
    @Override
    public String toString() {
        String cabecalhosVisiveis = cabecalhos.entrySet().stream()
                .map(entrada -> entrada.getKey() + "="
                        + (carregaCredencial(entrada.getKey()) ? VALOR_MASCARADO : entrada.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
        return "RequisicaoHttp[metodo=" + metodo + ", url=" + url + ", cabecalhos=" + cabecalhosVisiveis
                + ", corpo=" + corpo + ", timeout=" + timeout + "]";
    }

    private static boolean carregaCredencial(String nome) {
        return "x-api-key".equalsIgnoreCase(nome) || "authorization".equalsIgnoreCase(nome);
    }
}
