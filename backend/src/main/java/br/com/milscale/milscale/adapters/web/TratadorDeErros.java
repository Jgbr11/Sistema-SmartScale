package br.com.milscale.milscale.adapters.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErroResposta> dataInvalida(DateTimeParseException ex) {
        return ResponseEntity.badRequest().body(new ErroResposta("Data invalida: " + ex.getParsedString()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(NoSuchElementException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Registro nao encontrado";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResposta(msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResposta> argumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErroResposta(ex.getMessage() != null ? ex.getMessage() : "Requisicao invalida"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("Requisicao invalida");
        return ResponseEntity.badRequest().body(new ErroResposta(msg));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tipoErrado(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(new ErroResposta("Valor invalido para '" + ex.getName() + "'"));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErroResposta> conflitoDeVersao(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroResposta("Esse registro foi alterado por outra pessoa. Recarregue e tente de novo."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> violacaoDeIntegridade(DataIntegrityViolationException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErroResposta("Dado duplicado ou invalido (ex.: CPF/login ja cadastrado)"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ErroResposta("Corpo da requisicao invalido ou faltando campos"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResposta> acessoNegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErroResposta("Acesso negado"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> inesperado(Exception ex) {
        if (ex instanceof ErrorResponse er) {
            String detalhe = er.getBody().getDetail();
            return ResponseEntity.status(er.getStatusCode())
                    .body(new ErroResposta(detalhe != null ? detalhe : "Requisicao invalida"));
        }
        log.error("Erro inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroResposta("Erro interno. Tente de novo ou avise o suporte."));
    }
}
