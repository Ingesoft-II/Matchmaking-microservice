package com.killerprotocol.matchmaking.adapter.in.web;

import com.killerprotocol.matchmaking.domain.exception.AccesoDenegadoException;
import com.killerprotocol.matchmaking.domain.exception.CredencialesInvalidasException;
import com.killerprotocol.matchmaking.domain.exception.TransicionInvalidaException;
import com.killerprotocol.matchmaking.domain.exception.UsuarioYaExisteException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(IllegalArgumentException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> validationError(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Datos de entrada inválidos.";
        }
        return Map.of("error", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> unreadableMessage(HttpMessageNotReadableException exception) {
        return Map.of("error", "El cuerpo de la solicitud es inválido o tiene un formato incorrecto.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Map<String, String> unsupportedMediaType(HttpMediaTypeNotSupportedException exception) {
        return Map.of("error", "El cuerpo debe enviarse como application/json.");
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> unauthorized(CredencialesInvalidasException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> forbidden(AccesoDenegadoException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(NoSuchElementException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler({TransicionInvalidaException.class, UsuarioYaExisteException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(RuntimeException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> internalError(Exception exception) {
        return Map.of("error", "Error interno del servidor.");
    }
}
