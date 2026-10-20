package com.killerprotocol.matchmaking.domain.model;

import java.util.HashSet;
import java.util.List;

public record Team(List<PlayerId> players) {
    public static final int SIZE = 5;

    public Team {
        if (players == null || players.size() != SIZE) {
            throw new IllegalArgumentException("Equipo incompleto, deben ser 5 jugadores.");
        }
        if (players.stream().anyMatch(java.util.Objects::isNull) || new HashSet<>(players).size() != SIZE) {
            throw new IllegalArgumentException("El equipo requiere jugadores distintos y válidos.");
        }
        players = List.copyOf(players);
    }
}
