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
import org.springframework.web.ErrorResponse;
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
 * <p>Erro de cliente vai com a mensagem dele (é útil e não é interna). Erro de servidor vira
 * {@code "Erro interno"} e a exceção inteira fica no log.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String ERRO_INTERNO = "Erro interno";

    // POR QUE: StatusInvalidoException estende IllegalArgumentException, então um handler resolve
    // os dois, e a mensagem do cliente é útil.
    @ExceptionHandler({IllegalArgumentException.class, StatusInvalidoException.class})
    public ResponseEntity<Map<String, String>> requisicaoInvalida(RuntimeException e) {
        return corpo(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** JSON malformado no body: sem handler, caía no formato padrão do Spring. */
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

    // POR QUE: Fallback de 500. A exceção vai inteira para o log; o cliente recebe só
    // {@code "Erro interno"}. Cuidado clássico, verificado: um handler de {@code Exception} também
    // alcança as exceções que o Spring já classificou, porque
    // {@code ExceptionHandlerExceptionResolver} (ordem 0) tem precedência sobre
    // {@code ResponseStatusExceptionResolver} (ordem 1) e sobre o
    // {@code DefaultHandlerExceptionResolver}. Sem o desvio abaixo, um {@code 406} de
    // {@code Accept} incompatível viraria {@code 500 "Erro interno"} — trocar um erro de cliente
    // claramente identificado por um de servidor. Por isso, quando a exceção implementa
    // {@code ErrorResponse}, o status dela é respeitado e a mensagem também.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> erroInesperado(Exception e) {
        if (e instanceof ErrorResponse erroDeCliente) {
            LOG.debug("Erro de cliente não tratado especificamente: {}", e.toString());
            return corpo(HttpStatus.valueOf(erroDeCliente.getStatusCode().value()),
                    mensagemDe(erroDeCliente));
        }
        LOG.error("Erro não mapeado em {} {}", e.getClass().getName(), e.getMessage(), e);
        return corpo(HttpStatus.INTERNAL_SERVER_ERROR, ERRO_INTERNO);
    }

    // ResponseStatusException traz o motivo que o código passou; as demais implementações de
    // ErrorResponse trazem a descrição do próprio status, já útil e não detalhe interno.
    private static String mensagemDe(ErrorResponse erro) {
        String motivo = erro.getBody().getDetail();
        return motivo == null || motivo.isBlank() ? erro.getBody().getTitle() : motivo;
    }

    private static ResponseEntity<Map<String, String>> corpo(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("mensagem", mensagem));
    }
}
