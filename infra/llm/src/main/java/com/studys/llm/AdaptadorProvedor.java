package com.studys.llm;

public interface AdaptadorProvedor {

    TipoProvedor tipo();

    RequisicaoHttp montarRequisicao(Requisicao req, ConfiguracaoProvedor cfg, String credencial);

    Resposta lerResposta(RespostaHttp http);
}
