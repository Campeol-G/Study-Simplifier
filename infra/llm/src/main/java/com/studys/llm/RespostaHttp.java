package com.studys.llm;

public record RespostaHttp(int status, String corpo) {

    public RespostaHttp {
        corpo = corpo == null ? "" : corpo;
    }
}
