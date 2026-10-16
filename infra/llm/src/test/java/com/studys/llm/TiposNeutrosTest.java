package com.studys.llm;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TiposNeutrosTest {

    private static Tokens tokens() {
        return new Tokens(10, 5);
    }

    private static List<Mensagem> duasMensagens() {
        List<Mensagem> mensagens = new ArrayList<>();
        mensagens.add(new Mensagem(Papel.USUARIO, "ola"));
        mensagens.add(new Mensagem(Papel.ASSISTENTE, "oi"));
        return mensagens;
    }

    @Test
    @DisplayName("a lista de mensagens é copiada, então mudar a original depois não muda a requisição")
    void mensagensCopiadasNaoRefletemMudancaPosterior() {
        List<Mensagem> mensagens = duasMensagens();

        Requisicao requisicao = new Requisicao("modelo", 100, null, null, mensagens, Duration.ofSeconds(60));
        mensagens.add(new Mensagem(Papel.USUARIO, "tardia"));

        assertThat(requisicao.mensagens())
                .as("a requisicao guarda uma copia, entao o add posterior nao aparece")
                .hasSize(2);
    }

    @Test
    @DisplayName("raciocinio vazio vira string vazia, nunca nulo")
    void raciocinioNuloViraStringVazia() {
        Resposta resposta = new Resposta("texto", null, tokens());

        assertThat(resposta.raciocinio())
                .as("raciocinio ausente e ausencia de reasoning, nao um nulo que quebre o chamador")
                .isEqualTo("");
    }

    @Test
    @DisplayName("uma requisição sem esquema é válida: nem toda chamada pede saída estruturada")
    void requisicaoSemEsquemaEValida() {
        Requisicao requisicao = new Requisicao("modelo", 100, null, null, List.of(), Duration.ofSeconds(60));

        assertThat(requisicao.esquema())
                .as("sem esquema significa resposta livre, nao erro")
                .isNull();
    }
}
