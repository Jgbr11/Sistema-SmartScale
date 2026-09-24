package br.com.milscale.milscale.adapters.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Tratamento de erro centralizado - sem isso, qualquer entrada mal
 * formatada (data invalida, id que nao existe, campo obrigatorio
 * faltando) virava um 500 cru do Spring, sem mensagem util pro front.
 * Convertido para respostas HTTP previsiveis que o front sabe tratar.
 */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String, String>> dataInvalida(DateTimeParseException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Data invalida: " + ex.getParsedString()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> naoEncontrado(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", ex.getMessage() != null ? ex.getMessage() : "Requisicao invalida"));
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> violacaoDeIntegridade(org.springframework.dao.DataIntegrityViolationException ex) {
        // ex.: CPF ou login duplicado, campo obrigatorio nulo
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "Dado duplicado ou invalido (ex.: CPF/login ja cadastrado)"));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> corpoInvalido(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Corpo da requisicao invalido ou faltando campos"));
    }
}
