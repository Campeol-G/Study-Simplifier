package com.studys.llm;

public record ConfiguracaoProvedor(
        String id, String nome, TipoProvedor tipo, String urlBase, String modeloPadrao) {
}
