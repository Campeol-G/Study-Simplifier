package com.studys.api.web;

import com.studys.planengine.NodePlanoNaoEncontradoException;
import com.studys.plans.PlanoNaoEncontradoException;
import com.studys.plans.StatusInvalidoException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Corpo de erro da API: sempre {@code {"mensagem": ...}}, em todos os status.
 *
 * <p>Erro de cliente é mapeado com a mensagem dele, porque ela é útil e não é interna. Erro de
 * servidor vira {@code "Erro interno"} e a exceção completa vai para o log — o cliente não recebe
 * nem a mensagem, nem o nome da classe, nem a stack.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String ERRO_INTERNO = "Erro interno";

    /**
     * Os dois davam 400 com o mesmo corpo (achado A18 da auditoria): um handler só resolve.
     * {@code StatusInvalidoException} estende {@code IllegalArgumentException}, então cai aqui
     * junto com "Assunto é obrigatório".
     */
    @ExceptionHandler({IllegalArgumentException.class, StatusInvalidoException.class})
    public ResponseEntity<Map<String, String>> requisicaoInvalida(RuntimeException e) {
        return corpo(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** JSON malformado no body: sem handler, caía no formato padrão do Spring (achado A19). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException e) {
        LOG.debug("Body ilegível", e);
        return corpo(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido");
    }

    /** UUID inválido no path, por exemplo {@code GET /plans/nao-e-uuid}. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return corpo(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + e.getName());
    }

    /** Falha de bean validation, quando houver {@code @Valid} no controller. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacaoDeCampo(MethodArgumentNotValidException e) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .orElse("Requisição inválida");
        return corpo(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler({PlanoNaoEncontradoException.class, NodePlanoNaoEncontradoException.class})
    public ResponseEntity<Map<String, String>> naoEncontrado(RuntimeException e) {
        return corpo(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> recursoNaoEncontrado(NoResourceFoundException e) {
        return corpo(HttpStatus.NOT_FOUND, "Recurso não encontrado");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> metodoNaoSuportado(HttpRequestMethodNotSupportedException e) {
        return corpo(HttpStatus.METHOD_NOT_ALLOWED, "Método não suportado");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, String>> mediaTypeNaoSuportado(HttpMediaTypeNotSupportedException e) {
        return corpo(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de conteúdo não suportado");
    }

    /**
     * Fallback de 500 (achado A3). A exceção vai inteira para o log; o cliente recebe só
     * {@code "Erro interno"}.
     *
     * <p>Esta é também a rede que impede o handler de {@code Exception} de virar um aspirador: antes
     * de cair aqui, {@link #naoEncontrado(RuntimeException)} e os handlers de 4xx já capturaram o que
     * tem status de cliente, e o tratamento específico do Spring (que carrega o status na própria
     * exceção) tem precedência sobre o handler genérico do {@code @RestControllerAdvice}.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> erroInesperado(Exception e) {
        LOG.error("Erro não mapeado em {} {}", e.getClass().getName(), e.getMessage(), e);
        return corpo(HttpStatus.INTERNAL_SERVER_ERROR, ERRO_INTERNO);
    }

    private static ResponseEntity<Map<String, String>> corpo(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("mensagem", mensagem));
    }
}
