package com.studys.llm;

/**
 * O provedor não devolveu algo usável: ou recusou explicitamente a chamada (status 400 ou acima),
 * ou devolveu com status de sucesso um corpo que não dá para ler — vazio ou que não é JSON.
 * Carrega o status e o corpo cru, para a tradução de erros ler.
 */
public class ProvedorRecusou extends RuntimeException {

    private final int status;
    private final String corpo;

    public ProvedorRecusou(int status, String corpo) {
        super("o provedor não devolveu algo usável (status " + status + ")");
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
