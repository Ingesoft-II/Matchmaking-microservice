package com.killerprotocol.matchmaking.domain.model;

public record PartyId(String value) {

    public PartyId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador del party no puede ser nulo o vacío.");
        }
    }
}
