package com.studys.llm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.studys.llm.http.HttpTransporte;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HttpTransporteTest {

    private HttpServer servidor;
    private String base;
    private HttpTransporte transporte;

    private final AtomicReference<String> metodoVisto = new AtomicReference<>();
    private final AtomicReference<String> caminhoVisto = new AtomicReference<>();
    private final AtomicReference<String> autorizacaoVista = new AtomicReference<>();
    private final AtomicReference<String> corpoVisto = new AtomicReference<>();

    private volatile int statusResposta = 200;
    private volatile String corpoResposta = "";
    private volatile long atrasoMs = 0;

    @BeforeEach
    void servidorNoAr() throws IOException {
        statusResposta = 200;
        corpoResposta = "";
        atrasoMs = 0;
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", troca -> {
            metodoVisto.set(troca.getRequestMethod());
            caminhoVisto.set(troca.getRequestURI().getPath());
            autorizacaoVista.set(troca.getRequestHeaders().getFirst("Authorization"));
            corpoVisto.set(new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            if (atrasoMs > 0) {
                try {
                    Thread.sleep(atrasoMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            byte[] corpo = corpoResposta.getBytes(StandardCharsets.UTF_8);
            if (corpo.length == 0) {
                troca.sendResponseHeaders(statusResposta, -1);
            } else {
                troca.sendResponseHeaders(statusResposta, corpo.length);
                try (OutputStream saida = troca.getResponseBody()) {
                    saida.write(corpo);
                }
            }
            troca.close();
        });
        servidor.start();
        base = "http://127.0.0.1:" + servidor.getAddress().getPort();
        transporte = new HttpTransporte();
    }

    @AfterEach
    void servidorAbaixo() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    private static RequisicaoHttp post(String url, String corpo) {
        return new RequisicaoHttp(
                MetodoHttp.POST, url, Map.of("Authorization", "Bearer x"), corpo, Duration.ofSeconds(5));
    }

    private static RequisicaoHttp get(String url) {
        return new RequisicaoHttp(MetodoHttp.GET, url, Map.of(), "", Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("o método, a url e os cabeçalhos chegam no servidor")
    void metodoUrlECabecalhosChegamNoServidor() {
        transporte.enviar(post(base + "/recurso", "{}"));

        assertThat(metodoVisto.get())
                .as("o servidor recebe o mesmo metodo enviado")
                .isEqualTo("POST");
        assertThat(caminhoVisto.get())
                .as("o path da url chega intacto no servidor")
                .isEqualTo("/recurso");
        assertThat(autorizacaoVista.get())
                .as("o cabecalho de autorizacao atravessa o transporte")
                .isEqualTo("Bearer x");
    }

    @Test
    @DisplayName("o corpo chega intacto")
    void corpoChegaIntacto() {
        String corpo = "{\"saudacao\":\"olá\"}";

        transporte.enviar(post(base + "/recurso", corpo));

        assertThat(corpoVisto.get())
                .as("o corpo com acento chega byte a byte, sem recodificacao")
                .isEqualTo(corpo);
    }

    @Test
    @DisplayName("o status e o corpo voltam")
    void statusECorpoVoltam() {
        statusResposta = 429;
        corpoResposta = "limite excedido, tente de novo";

        RespostaHttp resposta = transporte.enviar(get(base + "/recurso"));

        assertThat(resposta.status())
                .as("o status de erro do servidor volta sem traducao")
                .isEqualTo(429);
        assertThat(resposta.corpo())
                .as("o corpo de erro volta como texto")
                .isEqualTo("limite excedido, tente de novo");
    }

    @Test
    @DisplayName("porta fechada vira erro nomeado, não exception crua")
    void portaFechadaViraErroNomeado() {
        int porta = servidor.getAddress().getPort();
        servidor.stop(0);

        RequisicaoHttp requisicao = get("http://127.0.0.1:" + porta + "/recurso");

        assertThatThrownBy(() -> transporte.enviar(requisicao))
                .as("porta sem servidor vira o erro nomeado do transporte")
                .isInstanceOf(FalhaDeTransporte.class)
                .hasMessageContaining("conexão")
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    @DisplayName("timeout estoura e diz qual era o limite")
    void timeoutEstouraEDizOLimite() {
        atrasoMs = 2000;
        RequisicaoHttp requisicao = new RequisicaoHttp(
                MetodoHttp.GET, base + "/lento", Map.of(), "", Duration.ofMillis(300));

        assertThatThrownBy(() -> transporte.enviar(requisicao))
                .as("resposta que demora alem do timeout vira erro nomeado")
                .isInstanceOf(FalhaDeTransporte.class)
                .hasMessageContaining("300");
    }

    @Test
    @DisplayName("corpo acima do teto é recusado")
    void corpoAcimaDoTetoERecusado() {
        corpoResposta = "x".repeat(3 * 1024 * 1024);

        assertThatThrownBy(() -> transporte.enviar(get(base + "/grande")))
                .as("resposta gigante e recusada antes de lotar a memoria")
                .isInstanceOf(FalhaDeTransporte.class)
                .hasMessageContaining("limite de tamanho");
    }

    @Test
    @DisplayName("erro com corpo que não é JSON volta cru")
    void erroNaoJsonVoltaCru() {
        statusResposta = 401;
        corpoResposta = "<html><body>não autorizado</body></html>";

        RespostaHttp resposta = transporte.enviar(get(base + "/recurso"));

        assertThat(resposta.status())
                .as("o status de erro volta sem traducao")
                .isEqualTo(401);
        assertThat(resposta.corpo())
                .as("o transporte nao parseia o corpo, devolve o html exato")
                .isEqualTo("<html><body>não autorizado</body></html>");
    }

    @Test
    @DisplayName("url malformada vira erro nomeado, não exceção crua de montagem")
    void urlMalformadaViraErroNomeado() {
        String url = "http://127.0.0.1:1/recurso com espaço";
        RequisicaoHttp requisicao = get(url);

        assertThatThrownBy(() -> transporte.enviar(requisicao))
                .as("url digitada à mão que não parseia vira o erro nomeado do transporte, não "
                        + "IllegalArgumentException crua")
                .isInstanceOf(FalhaDeTransporte.class)
                .hasMessageContaining(url);
    }

    @Test
    @DisplayName("resposta sem corpo volta como string vazia")
    void respostaSemCorpoVoltaVazia() {
        statusResposta = 204;
        corpoResposta = "";

        RespostaHttp resposta = transporte.enviar(get(base + "/recurso"));

        assertThat(resposta.corpo())
                .as("resposta sem corpo vira string vazia, nunca nulo e sem excecao")
                .isEqualTo("");
    }
}
