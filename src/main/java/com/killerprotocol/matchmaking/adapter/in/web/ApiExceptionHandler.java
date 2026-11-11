package com.killerprotocol.matchmaking.adapter.in.web;

import com.killerprotocol.matchmaking.domain.exception.CredencialesInvalidasException;
import com.killerprotocol.matchmaking.domain.exception.TransicionInvalidaException;
import com.killerprotocol.matchmaking.domain.exception.UsuarioYaExisteException;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, org.springframework.web.bind.MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(Exception exception) { return Map.of("error", exception.getMessage()); }

    @ExceptionHandler(CredencialesInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> unauthorized(CredencialesInvalidasException exception) { return Map.of("error", exception.getMessage()); }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(Exception exception) { return Map.of("error", exception.getMessage()); }

    @ExceptionHandler({TransicionInvalidaException.class, UsuarioYaExisteException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(RuntimeException exception) { return Map.of("error", exception.getMessage()); }
}
