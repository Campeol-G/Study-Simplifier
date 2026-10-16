package com.studys.llm;

import static org.assertj.core.api.Assertions.assertThat;

import com.studys.llm.http.HttpTransporte;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransporteFalsoTest {

    private HttpServer servidor;
    private String base;

    @BeforeEach
    void servidorNoAr() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", troca -> {
            byte[] corpo = "corpo real".getBytes(StandardCharsets.UTF_8);
            troca.sendResponseHeaders(200, corpo.length);
            try (OutputStream saida = troca.getResponseBody()) {
                saida.write(corpo);
            }
            troca.close();
        });
        servidor.start();
        base = "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    @AfterEach
    void servidorAbaixo() {
        servidor.stop(0);
    }

    private static RequisicaoHttp post(String url, String corpo) {
        return new RequisicaoHttp(
                MetodoHttp.POST, url, Map.of("Authorization", "Bearer x"), corpo, Duration.ofSeconds(5));
    }

    private String corpoVia(Transporte transporte, String url) {
        return transporte.enviar(post(url, "{}")).corpo();
    }

    @Test
    @DisplayName("o falso devolve exatamente a resposta configurada")
    void falsoDevolveRespostaConfigurada() {
        TransporteFalso falso = new TransporteFalso();
        falso.responder(new RespostaHttp(200, "{\"a\":1}"));

        RespostaHttp resposta = falso.enviar(post("http://127.0.0.1/recurso", "{}"));

        assertThat(resposta)
                .as("o falso devolve exatamente a resposta configurada")
                .isEqualTo(new RespostaHttp(200, "{\"a\":1}"));
    }

    @Test
    @DisplayName("o falso guarda a requisição que recebeu")
    void falsoGuardaRequisicaoRecebida() {
        TransporteFalso falso = new TransporteFalso();
        falso.responder(new RespostaHttp(200, "ok"));
        RequisicaoHttp requisicao = post("http://127.0.0.1/recurso", "{\"a\":1}");

        falso.enviar(requisicao);

        assertThat(falso.ultimaRequisicao().url())
                .as("o falso guarda a url da requisicao recebida")
                .isEqualTo("http://127.0.0.1/recurso");
    }

    @Test
    @DisplayName("o mesmo teste passa com o falso e com o transporte real")
    void mesmoTestePassaComFalsoEComReal() {
        TransporteFalso falso = new TransporteFalso();
        falso.responder(new RespostaHttp(200, "corpo real"));

        String peloFalso = corpoVia(falso, "http://127.0.0.1/recurso");
        String peloReal = corpoVia(new HttpTransporte(), base + "/recurso");

        assertThat(peloFalso)
                .as("o falso e o transporte real devolvem o mesmo corpo")
                .isEqualTo(peloReal);
    }
}
