package com.studys.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        Resposta resposta = new Resposta("texto", null, null, tokens());

        assertThat(resposta.raciocinio())
                .as("raciocinio ausente e ausencia de reasoning, nao um nulo que quebre o chamador")
                .isEqualTo("");
    }

    @Test
    @DisplayName("motivo de parada nulo vira string vazia, nunca nulo")
    void motivoDeParadaNuloViraStringVazia() {
        Resposta resposta = new Resposta("texto", null, null, tokens());

        assertThat(resposta.motivoDeParada())
                .as("motivo de parada ausente e ausencia de sinal, nao um nulo que quebre o chamador")
                .isEqualTo("");
    }

    @Test
    @DisplayName("o toString esconde o valor dos cabeçalhos que carregam credencial")
    void toStringEscondeCredencialDosCabecalhos() {
        RequisicaoHttp http = new RequisicaoHttp(
                MetodoHttp.POST,
                "https://api.anthropic.com/v1/messages",
                Map.of(
                        "X-Api-Key", "segredo-da-chave",
                        "authorization", "Bearer segredo-do-bearer",
                        "anthropic-version", "2023-06-01"),
                "{}",
                Duration.ofSeconds(5));

        String impresso = http.toString();

        assertThat(impresso)
                .as("nem a chave de api nem o bearer token aparecem no texto impresso")
                .doesNotContain("segredo-da-chave")
                .doesNotContain("segredo-do-bearer");
        assertThat(impresso)
                .as("cabeçalho sem credencial continua visível para depurar")
                .contains("anthropic-version=2023-06-01");
    }

    @Test
    @DisplayName("uma requisição sem esquema é válida: nem toda chamada pede saída estruturada")
    void requisicaoSemEsquemaEValida() {
        Requisicao requisicao = new Requisicao("modelo", 100, null, null, List.of(), Duration.ofSeconds(60));

        assertThat(requisicao.esquema())
                .as("sem esquema significa resposta livre, nao erro")
                .isNull();
    }

    @Test
    @DisplayName("requisição com mensagens nula lança exceção que nomeia o campo")
    void requisicaoComMensagensNulaLancaExcecaoComMensagem() {
        assertThatThrownBy(() -> new Requisicao(
                "modelo", 100, null, null, null, Duration.ofSeconds(60)))
                .as("lista de mensagens é obrigatória e o erro deve nomear o campo")
                .isInstanceOf(NullPointerException.class)
                .hasMessage("mensagens é obrigatório");
    }
}
