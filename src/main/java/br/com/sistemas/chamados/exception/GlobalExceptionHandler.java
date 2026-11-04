package br.com.sistemas.chamados.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), new ArrayList<>());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegraNegocio(RegraNegocioException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), new ArrayList<>());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException ex) {
        List<String> erros = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(campo -> erros.add(campo.getField() + ": " + campo.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Dados inválidos", erros);
    }

    private ResponseEntity<ErroResposta> responder(HttpStatus status, String mensagem, List<String> detalhes) {
        ErroResposta corpo = new ErroResposta(LocalDateTime.now(), status.value(), mensagem, detalhes);
        return ResponseEntity.status(status).body(corpo);
    }
}
