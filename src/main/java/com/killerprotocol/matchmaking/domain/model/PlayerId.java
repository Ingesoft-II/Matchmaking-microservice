package com.killerprotocol.matchmaking.domain.model;

public record PlayerId(String value) {

    public PlayerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador del jugador no puede ser nulo o vacío.");
        }
    }
}
