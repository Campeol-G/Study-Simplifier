package com.studys.llm.anthropic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.studys.llm.ProvedorRecusou;
import com.studys.llm.Resposta;
import com.studys.llm.RespostaHttp;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Exercita {@link AdaptadorAnthropic#lerResposta} sobre
 * {@code gravado/anthropic-resposta.json}, uma captura sintética escrita à mão —
 * ainda não há chave real para gravar uma resposta verdadeira — fiel à forma
 * documentada da API. Quando houver chamada real, trocar o arquivo por uma
 * captura verdadeira.
 */
class AdaptadorAnthropicRespostaTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final AdaptadorAnthropic adaptador = new AdaptadorAnthropic();

    private static String corpoGravado() throws Exception {
        Path gravado = Path.of(
                AdaptadorAnthropicRespostaTest.class.getResource("/gravado/anthropic-resposta.json").toURI());
        return Files.readString(gravado, StandardCharsets.UTF_8);
    }

    private static String comBlocoExtra(String corpo, String tipo, String campo, String valor) {
        ObjectNode raiz = (ObjectNode) JSON.readTree(corpo);
        ArrayNode conteudo = (ArrayNode) raiz.get("content");
        ObjectNode extra = JSON.createObjectNode();
        extra.put("type", tipo);
        extra.put(campo, valor);
        conteudo.add(extra);
        return JSON.writeValueAsString(raiz);
    }

    @Test
    @DisplayName("o texto é a junção dos blocos de texto")
    void textoEJuncaoDosBlocosDeTexto() throws Exception {
        String corpo = comBlocoExtra(corpoGravado(), "text", "text", "Posso resumir documentos também.");

        Resposta resposta = adaptador.lerResposta(new RespostaHttp(200, corpo));

        assertThat(resposta.texto())
                .as("os blocos de texto saem na ordem, separados por linha em branco")
                .isEqualTo("Olá! Como posso ajudar?\n\nPosso resumir documentos também.");
    }

    @Test
    @DisplayName("o raciocínio é a junção dos blocos de thinking")
    void raciocinioEJuncaoDosBlocosDeThinking() throws Exception {
        Resposta resposta = adaptador.lerResposta(new RespostaHttp(200, corpoGravado()));

        assertThat(resposta.raciocinio())
                .as("o bloco thinking preenche o raciocinio")
                .isEqualTo("O usuário cumprimentou; basta responder de forma breve e cordial.");
    }

    @Test
    @DisplayName("o uso vem de input_tokens e output_tokens")
    void usoVemDeInputTokensEOutputTokens() throws Exception {
        Resposta resposta = adaptador.lerResposta(new RespostaHttp(200, corpoGravado()));

        assertThat(resposta.uso().entrada()).as("a entrada vem de input_tokens").isEqualTo(12);
        assertThat(resposta.uso().saida()).as("a saida vem de output_tokens").isEqualTo(34);
    }

    @Test
    @DisplayName("resposta sem bloco de texto vira string vazia, não exceção")
    void semBlocoDeTextoViraStringVazia() {
        String corpo = """
                {"content": [{"type": "thinking", "thinking": "Só pensei, nada a dizer."}], \
                "usage": {"input_tokens": 5, "output_tokens": 1}}\
                """;

        Resposta resposta = adaptador.lerResposta(new RespostaHttp(200, corpo));

        assertThat(resposta.texto()).as("sem bloco de texto, o texto e vazio").isEqualTo("");
        assertThat(resposta.raciocinio())
                .as("o thinking continua preenchendo o raciocinio")
                .isEqualTo("Só pensei, nada a dizer.");
    }

    @Test
    @DisplayName("provedor recusando vira ProvedorRecusou com o corpo cru")
    void recusaViraProvedorRecusouComCorpoCru() {
        String corpo = """
                {"type": "error", "error": {"type": "authentication_error", \
                "message": "invalid x-api-key"}}\
                """;

        assertThatThrownBy(() -> adaptador.lerResposta(new RespostaHttp(401, corpo)))
                .as("o erro do provedor nao e lido como mensagem")
                .isInstanceOfSatisfying(ProvedorRecusou.class, recusou -> {
                    assertThat(recusou.status()).as("o status original viaja na excecao").isEqualTo(401);
                    assertThat(recusou.corpo()).as("o corpo cru viaja na excecao").isEqualTo(corpo);
                });
    }
}
