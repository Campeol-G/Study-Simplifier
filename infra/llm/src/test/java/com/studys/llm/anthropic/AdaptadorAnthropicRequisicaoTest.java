package com.studys.llm.anthropic;

import static org.assertj.core.api.Assertions.assertThat;

import com.studys.llm.ConfiguracaoProvedor;
import com.studys.llm.Mensagem;
import com.studys.llm.MetodoHttp;
import com.studys.llm.Papel;
import com.studys.llm.Requisicao;
import com.studys.llm.RequisicaoHttp;
import com.studys.llm.TipoProvedor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class AdaptadorAnthropicRequisicaoTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final String CREDENCIAL = "chave-de-teste";

    private static Requisicao requisicaoDeTeste(Double temperatura) {
        return new Requisicao(
                "claude-sonnet-4-5",
                1024,
                temperatura,
                null,
                List.of(
                        new Mensagem(Papel.SISTEMA, "primeiro sistema"),
                        new Mensagem(Papel.SISTEMA, "segundo sistema"),
                        new Mensagem(Papel.USUARIO, "ola"),
                        new Mensagem(Papel.ASSISTENTE, "oi")),
                Duration.ofSeconds(180));
    }

    private static ConfiguracaoProvedor configuracao(String urlBase) {
        return new ConfiguracaoProvedor(
                "anthropic", "Anthropic", TipoProvedor.ANTHROPIC, urlBase, "claude-sonnet-4-5");
    }

    private static ConfiguracaoProvedor configuracao(String urlBase, String modeloPadrao) {
        return new ConfiguracaoProvedor(
                "anthropic", "Anthropic", TipoProvedor.ANTHROPIC, urlBase, modeloPadrao);
    }

    private static RequisicaoHttp montada(String urlBase, Double temperatura) {
        return new AdaptadorAnthropic()
                .montarRequisicao(requisicaoDeTeste(temperatura), configuracao(urlBase), CREDENCIAL);
    }

    private static JsonNode corpoDe(RequisicaoHttp http) {
        return JSON.readTree(http.corpo());
    }

    @Test
    @DisplayName("a url base com barra no fim não vira barra dupla")
    void barraFinalNaoViraBarraDupla() {
        RequisicaoHttp http = montada("https://api.anthropic.com/", 0.7);

        assertThat(http.url())
                .as("a barra final da base e somada a uma so barra do caminho")
                .isEqualTo("https://api.anthropic.com/v1/messages");
        assertThat(http.metodo()).as("montar mensagem anthropic e POST").isEqualTo(MetodoHttp.POST);
        assertThat(http.timeout())
                .as("o prazo e o do chamador, nao uma constante do adaptador")
                .isEqualTo(Duration.ofSeconds(180));
    }

    @Test
    @DisplayName("a chave vai em x-api-key e a versão da API em anthropic-version")
    void cabecalhosLevamChaveEVersao() {
        RequisicaoHttp http = montada("https://api.anthropic.com", 0.7);

        assertThat(http.cabecalhos())
                .as("autenticacao por x-api-key com a versao fixada da API")
                .containsEntry("x-api-key", CREDENCIAL)
                .containsEntry("anthropic-version", "2023-06-01")
                .containsEntry("content-type", "application/json");
    }

    @Test
    @DisplayName("o prompt de sistema vai no campo system do topo, não em messages")
    void sistemaVaiNoTopoNaoEmMessages() {
        JsonNode corpo = corpoDe(montada("https://api.anthropic.com", 0.7));

        assertThat(corpo.get("model").asString()).as("o modelo pedido vai no corpo").isEqualTo("claude-sonnet-4-5");
        assertThat(corpo.get("max_tokens").asInt()).as("o limite pedido vai no corpo").isEqualTo(1024);
        assertThat(corpo.get("system").asString()).as("o topo carrega o sistema").isNotBlank();
        List<String> papeis = new ArrayList<>();
        corpo.get("messages").forEach(mensagem -> papeis.add(mensagem.get("role").asString()));
        assertThat(papeis)
                .as("messages so conhece user e assistant, nunca system")
                .containsExactly("user", "assistant");
    }

    @Test
    @DisplayName("vários prompts de sistema viram um só, separados por linha em branco")
    void variosSistemasViramUmSo() {
        JsonNode corpo = corpoDe(montada("https://api.anthropic.com", 0.7));

        assertThat(corpo.get("system").asString())
                .as("sistemas somados em ordem, separados por linha em branco")
                .isEqualTo("primeiro sistema\n\nsegundo sistema");
    }

    @Test
    @DisplayName("temperatura nula não vai para o corpo")
    void temperaturaNulaNaoVaiParaOCorpo() {
        JsonNode corpo = corpoDe(montada("https://api.anthropic.com", null));

        assertThat(corpo.has("temperature"))
                .as("temperatura ausente significa chave ausente, nao nula")
                .isFalse();
    }

    @Test
    @DisplayName("modelo nulo na requisição cai para o modelo padrão do provedor")
    void modeloNuloCaiParaOModeloPadrao() {
        Requisicao semModelo = new Requisicao(
                null, 1024, null, null, List.of(new Mensagem(Papel.USUARIO, "ola")), Duration.ofSeconds(180));

        RequisicaoHttp http = new AdaptadorAnthropic()
                .montarRequisicao(
                        semModelo, configuracao("https://api.anthropic.com", "claude-opus-4-1"), CREDENCIAL);

        assertThat(corpoDe(http).get("model").asString())
                .as("sem modelo pedido, o corpo carrega o modelo padrao da configuracao, nao um nulo")
                .isEqualTo("claude-opus-4-1");
    }

    @Test
    @DisplayName("requisição sem prompt de sistema não leva a chave system no corpo")
    void semSistemaNaoLevaChaveSystem() {
        Requisicao semSistema = new Requisicao(
                "claude-sonnet-4-5",
                1024,
                null,
                null,
                List.of(new Mensagem(Papel.USUARIO, "ola")),
                Duration.ofSeconds(180));

        JsonNode corpo = corpoDe(new AdaptadorAnthropic()
                .montarRequisicao(semSistema, configuracao("https://api.anthropic.com"), CREDENCIAL));

        assertThat(corpo.has("system"))
                .as("sem prompt de sistema, a chave system fica ausente, nao vazia nem nula")
                .isFalse();
    }

    @Test
    @DisplayName("a requisição inteira é a que está gravada")
    void corpoInteiroIgualAoGravado() throws Exception {
        RequisicaoHttp http = montada("https://api.anthropic.com", 0.7);

        Path gravado = Path.of(
                getClass().getResource("/gravado/anthropic-requisicao.json").toURI());
        String esperado = Files.readString(gravado, StandardCharsets.UTF_8);
        if (esperado.endsWith("\n")) {
            esperado = esperado.substring(0, esperado.length() - 1);
        }

        assertThat(http.corpo())
                .as("o corpo e byte a byte o gravado, sem contar a quebra final do arquivo")
                .isEqualTo(esperado);
    }
}
