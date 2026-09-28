package com.studys.api.web;

import com.studys.identity.CredenciaisInvalidasException;
import com.studys.identity.EmailEmUsoException;
import com.studys.identity.TokenInvalidoException;
import com.studys.plans.PlanoNaoEncontradoException;
import com.studys.plans.StatusInvalidoException;
import com.studys.plans.UsuarioNaoEncontradoException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> validacao(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(StatusInvalidoException.class)
    public ResponseEntity<Map<String, String>> statusInvalido(StatusInvalidoException e) {
        return ResponseEntity.status(400).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler({CredenciaisInvalidasException.class, TokenInvalidoException.class})
    public ResponseEntity<Map<String, String>> naoAutenticado(RuntimeException e) {
        return ResponseEntity.status(401).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(PlanoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(PlanoNaoEncontradoException e) {
        return ResponseEntity.status(404).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> sessaoInvalida(UsuarioNaoEncontradoException e) {
        return ResponseEntity.status(401).body(Map.of("mensagem", e.getMessage()));
    }

    @ExceptionHandler(EmailEmUsoException.class)
    public ResponseEntity<Map<String, String>> conflito(EmailEmUsoException e) {
        return ResponseEntity.status(409).body(Map.of("mensagem", e.getMessage()));
    }
}
