package com.killerprotocol.matchmaking.domain.model;

import java.time.Instant;

public record Partida(PartidaId id, GameMode mode, String map, String region,
                      String serverAddress, Team teamA, Team teamB, Instant createdAt) {
    public Partida {
        if (id == null || mode == null || teamA == null || teamB == null || createdAt == null) {
            throw new IllegalArgumentException("La partida requiere id, modo, equipos e instante de creación.");
        }
        if (map == null || map.isBlank() || region == null || region.isBlank()
                || serverAddress == null || serverAddress.isBlank()) {
            throw new IllegalArgumentException("La partida requiere mapa, región y servidor.");
        }
        if (teamA.players().stream().anyMatch(teamB.players()::contains)) {
            throw new IllegalArgumentException("Un jugador no puede estar en ambos equipos.");
        }
    }
}
