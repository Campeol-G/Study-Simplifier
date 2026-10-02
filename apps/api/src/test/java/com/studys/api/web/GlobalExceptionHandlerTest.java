package com.studys.api.web;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O fallback de {@code Exception} nao pode virar um aspirador.
 *
 * <p>As excecoes que o Spring ja classifica carregam o status de cliente dentro delas, via
 * {@code ErrorResponse}. Um handler de {@code Exception} e escolhido para elas quando nenhuma outra
 * opcao casa — e ai o erro de cliente viraria 500 com mensagem {@code "Erro interno"}, que e pior do
 * que o problema que o fallback resolve.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("excecao de cliente com status proprio mantem o status e nao vira Erro interno")
    void excecaoDeClienteNaoVira500() {
        ResponseEntity<Map<String, String>> resposta =
                handler.erroInesperado(new ResponseStatusException(HttpStatus.CONFLICT, "conflito"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody()).containsEntry("mensagem", "conflito");
        assertThat(resposta.getBody()).doesNotContainEntry("mensagem", "Erro interno");
    }

    @Test
    @DisplayName("status 4xx do framework preserva o status original")
    void statusDoFrameworkPreservado() {
        for (HttpStatus status : new HttpStatus[]{
                HttpStatus.BAD_REQUEST,
                HttpStatus.UNAUTHORIZED,
                HttpStatus.FORBIDDEN,
                HttpStatus.NOT_FOUND,
                HttpStatus.CONFLICT,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                HttpStatus.NOT_ACCEPTABLE,
                HttpStatus.METHOD_NOT_ALLOWED,
                HttpStatus.valueOf(422)}) {
            ResponseEntity<Map<String, String>> resposta =
                    handler.erroInesperado(new ResponseStatusException(status));

            assertThat(resposta.getStatusCode().value()).as("codigo de %s", status).isEqualTo(status.value());
            assertThat(resposta.getBody()).as("corpo de %s", status)
                    .containsEntry("mensagem", status.getReasonPhrase());
        }
    }

    @Test
    @DisplayName("excecao sem status proprio vira 500 com mensagem generica")
    void excecaoSemStatusVira500() {
        ResponseEntity<Map<String, String>> resposta =
                handler.erroInesperado(new IllegalStateException("boom: segredo em /caminho/secreto"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(resposta.getBody()).containsExactly(Map.entry("mensagem", "Erro interno"));
    }
}
