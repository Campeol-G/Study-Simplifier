package com.studys.llm;

public class ProvedorRecusou extends RuntimeException {

    private final int status;
    private final String corpo;

    public ProvedorRecusou(int status, String corpo) {
        super("o provedor recusou a chamada com status " + status);
        this.status = status;
        this.corpo = corpo;
    }

    public int status() {
        return status;
    }

    public String corpo() {
        return corpo;
    }
}
