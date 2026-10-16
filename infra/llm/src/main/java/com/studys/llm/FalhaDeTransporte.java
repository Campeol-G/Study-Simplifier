package com.studys.llm;

public class FalhaDeTransporte extends RuntimeException {

    public FalhaDeTransporte(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
