package com.killerprotocol.matchmaking.domain.exception;

public class UsuarioYaExisteException extends RuntimeException {

    public UsuarioYaExisteException(String message) {
        super(message);
    }
}
