package com.killerprotocol.matchmaking.domain.exception;

public class TransicionInvalidaException extends RuntimeException {

    private final String estado;
    private final String operacion;

    public TransicionInvalidaException(String estado, String operacion) {
        super("Transición inválida: no se puede aplicar '" + operacion + "' en estado '" + estado + "'.");
        this.estado = estado;
        this.operacion = operacion;
    }

    public String estado() {
        return estado;
    }

    public String operacion() {
        return operacion;
    }
}
