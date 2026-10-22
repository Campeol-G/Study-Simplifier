package com.studys.llm;

public record Resposta(String texto, String raciocinio, String motivoDeParada, Tokens uso) {

    public Resposta {
        if (raciocinio == null) {
            raciocinio = "";
        }
        if (motivoDeParada == null) {
            motivoDeParada = "";
        }
        if (texto == null) {
            texto = "";
        }
        if (uso == null) {
            uso = new Tokens(0, 0);
        }
    }
}
