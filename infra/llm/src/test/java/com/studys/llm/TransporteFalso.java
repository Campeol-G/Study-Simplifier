package com.studys.llm;

public class TransporteFalso implements Transporte {

    private RespostaHttp resposta = new RespostaHttp(200, "");
    private RuntimeException erro;
    private RequisicaoHttp ultimaRequisicao;

    public void responder(RespostaHttp resposta) {
        this.resposta = resposta;
    }

    public void falhar(RuntimeException erro) {
        this.erro = erro;
    }

    public RequisicaoHttp ultimaRequisicao() {
        return ultimaRequisicao;
    }

    @Override
    public RespostaHttp enviar(RequisicaoHttp requisicao) {
        ultimaRequisicao = requisicao;
        if (erro != null) {
            throw erro;
        }
        return resposta;
    }
}
