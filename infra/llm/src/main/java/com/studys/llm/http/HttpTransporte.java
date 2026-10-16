package com.studys.llm.http;

import com.studys.llm.FalhaDeTransporte;
import com.studys.llm.RequisicaoHttp;
import com.studys.llm.RespostaHttp;
import com.studys.llm.Transporte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;

public class HttpTransporte implements Transporte {

    public static final int LIMITE_CORPO_BYTES = 1024 * 1024;

    private static final int TRECHO_LEITURA_BYTES = 8192;

    private final HttpClient cliente;

    public HttpTransporte() {
        this.cliente = HttpClient.newHttpClient();
    }

    @Override
    public RespostaHttp enviar(RequisicaoHttp requisicao) {
        HttpRequest.Builder montador = HttpRequest.newBuilder()
                .uri(URI.create(requisicao.url()))
                .timeout(requisicao.timeout());
        requisicao.cabecalhos().forEach(montador::header);
        switch (requisicao.metodo()) {
            case GET -> montador.GET();
            case POST -> montador.POST(
                    HttpRequest.BodyPublishers.ofString(requisicao.corpo(), StandardCharsets.UTF_8));
        }
        try {
            HttpResponse<InputStream> resposta =
                    cliente.send(montador.build(), HttpResponse.BodyHandlers.ofInputStream());
            return new RespostaHttp(resposta.statusCode(), lerComLimite(resposta.body()));
        } catch (HttpConnectTimeoutException e) {
            throw new FalhaDeTransporte("falha de conexão com " + requisicao.url(), e);
        } catch (HttpTimeoutException e) {
            throw new FalhaDeTransporte(
                    "tempo limite de " + requisicao.timeout().toMillis() + " ms excedido para "
                            + requisicao.url(),
                    e);
        } catch (ConnectException e) {
            throw new FalhaDeTransporte("falha de conexão com " + requisicao.url(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FalhaDeTransporte("requisição para " + requisicao.url() + " interrompida", e);
        } catch (IOException e) {
            throw new FalhaDeTransporte("falha de transporte para " + requisicao.url(), e);
        }
    }

    private static String lerComLimite(InputStream corpo) throws IOException {
        try (corpo) {
            ByteArrayOutputStream acumulado = new ByteArrayOutputStream();
            byte[] trecho = new byte[TRECHO_LEITURA_BYTES];
            int lidos;
            int total = 0;
            while ((lidos = corpo.read(trecho)) != -1) {
                total += lidos;
                if (total > LIMITE_CORPO_BYTES) {
                    throw new FalhaDeTransporte(
                            "corpo da resposta excedeu o limite de tamanho de " + LIMITE_CORPO_BYTES
                                    + " bytes",
                            null);
                }
                acumulado.write(trecho, 0, lidos);
            }
            return acumulado.toString(StandardCharsets.UTF_8);
        }
    }
}
