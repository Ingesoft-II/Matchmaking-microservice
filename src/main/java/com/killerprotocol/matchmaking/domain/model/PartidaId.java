package com.killerprotocol.matchmaking.domain.model;

public record PartidaId(String value) {

    public PartidaId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador de la partida no puede ser nulo o vacío.");
        }
    }
}
