package com.studys.llm;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public record RequisicaoHttp(
        MetodoHttp metodo, String url, Map<String, String> cabecalhos, String corpo, Duration timeout) {

    public RequisicaoHttp {
        Objects.requireNonNull(metodo, "metodo é obrigatório");
        Objects.requireNonNull(url, "url é obrigatória");
        Objects.requireNonNull(timeout, "timeout é obrigatório");
        cabecalhos = cabecalhos == null ? Map.of() : Map.copyOf(cabecalhos);
        corpo = corpo == null ? "" : corpo;
    }
}
