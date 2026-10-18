package com.killerprotocol.matchmaking.domain.model;

public record TicketId(String value) {

    public TicketId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador del ticket no puede ser nulo o vacío.");
        }
    }
}
