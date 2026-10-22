package com.studys.llm;

import java.util.ArrayDeque;
import java.util.Deque;

public class TransporteFalso implements Transporte {

    private RespostaHttp resposta = new RespostaHttp(200, "");
    private final Deque<RespostaHttp> fila = new ArrayDeque<>();
    private RuntimeException erro;
    private RequisicaoHttp ultimaRequisicao;
    private int chamadas;

    public void responder(RespostaHttp resposta) {
        this.resposta = resposta;
    }

    /**
     * Enfileira uma resposta a ser devolvida na próxima chamada. Útil para simular
     * sequências como "429, depois 200" na entrega 4.
     */
    public void enfileirar(RespostaHttp resposta) {
        fila.addLast(resposta);
    }

    public void falhar(RuntimeException erro) {
        this.erro = erro;
    }

    public RequisicaoHttp ultimaRequisicao() {
        return ultimaRequisicao;
    }

    /**
     * Quantas vezes {@link #enviar} foi chamado. Útil para afirmar o teto de tentativas.
     */
    public int chamadas() {
        return chamadas;
    }

    @Override
    public RespostaHttp enviar(RequisicaoHttp requisicao) {
        ultimaRequisicao = requisicao;
        chamadas++;
        if (erro != null) {
            throw erro;
        }
        RespostaHttp daFila = fila.pollFirst();
        return daFila != null ? daFila : resposta;
    }
}
