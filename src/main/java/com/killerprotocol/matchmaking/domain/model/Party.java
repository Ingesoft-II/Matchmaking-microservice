package com.killerprotocol.matchmaking.domain.model;

import java.util.HashSet;
import java.util.List;

public record Party(PartyId id, List<PlayerId> members) {

    public Party {
        if (id == null) {
            throw new IllegalArgumentException("El party requiere un identificador.");
        }
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("Un party debe tener al menos 1 jugador.");
        }
        if (members.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Un party no puede contener jugadores nulos.");
        }
        if (members.size() > 5) {
            throw new IllegalArgumentException("Un party no puede tener más de 5 jugadores.");
        }
        if (new HashSet<>(members).size() != members.size()) {
            throw new IllegalArgumentException("Un party no puede tener jugadores duplicados.");
        }
        members = List.copyOf(members);
    }

    public int size() {
        return members.size();
    }
}
