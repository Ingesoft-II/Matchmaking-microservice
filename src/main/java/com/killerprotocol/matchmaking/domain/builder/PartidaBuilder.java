package com.killerprotocol.matchmaking.domain.builder;

import com.killerprotocol.matchmaking.domain.model.GameMode;
import com.killerprotocol.matchmaking.domain.model.Partida;
import com.killerprotocol.matchmaking.domain.model.PartidaId;
import com.killerprotocol.matchmaking.domain.model.Team;
import java.time.Instant;

public interface PartidaBuilder {
    PartidaBuilder id(PartidaId id);
    PartidaBuilder mode(GameMode mode);
    PartidaBuilder map(String map);
    PartidaBuilder region(String region);
    PartidaBuilder serverAddress(String serverAddress);
    PartidaBuilder teams(Team teamA, Team teamB);
    PartidaBuilder createdAt(Instant createdAt);
    Partida build();
}
