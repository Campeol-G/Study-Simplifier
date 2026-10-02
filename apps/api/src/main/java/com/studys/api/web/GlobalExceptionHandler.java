package com.studys.api.web;

import com.studys.planengine.NodePlanoNaoEncontradoException;
import com.studys.plans.PlanoNaoEncontradoException;
import com.studys.plans.StatusInvalidoException;
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

    @ExceptionHandler({PlanoNaoEncontradoException.class, NodePlanoNaoEncontradoException.class})
    public ResponseEntity<Map<String, String>> naoEncontrado(RuntimeException e) {
        return ResponseEntity.status(404).body(Map.of("mensagem", e.getMessage()));
    }

}
