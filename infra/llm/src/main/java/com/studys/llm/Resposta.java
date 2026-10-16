package com.studys.llm;

public record Resposta(String texto, String raciocinio, Tokens uso) {

    public Resposta {
        if (raciocinio == null) {
            raciocinio = "";
        }
    }
}
