package com.killerprotocol.matchmaking.domain.builder;

import com.killerprotocol.matchmaking.domain.model.*;
import java.time.Instant;

public final class ArcadePartidaBuilder implements PartidaBuilder {
    private PartidaId id;
    private String map;
    private String region;
    private String serverAddress;
    private Team teamA;
    private Team teamB;
    private Instant createdAt;
    @Override public PartidaBuilder id(PartidaId value) { id = value; return this; }
    @Override public PartidaBuilder mode(GameMode value) { if (value != GameMode.ARCADE) throw new IllegalArgumentException("El builder arcade requiere modo ARCADE."); return this; }
    @Override public PartidaBuilder map(String value) { map = value; return this; }
    @Override public PartidaBuilder region(String value) { region = value; return this; }
    @Override public PartidaBuilder serverAddress(String value) { serverAddress = value; return this; }
    @Override public PartidaBuilder teams(Team a, Team b) { teamA = a; teamB = b; return this; }
    @Override public PartidaBuilder createdAt(Instant value) { createdAt = value; return this; }
    @Override public Partida build() { return new Partida(id, GameMode.ARCADE, map, region, serverAddress, teamA, teamB, createdAt); }
}
