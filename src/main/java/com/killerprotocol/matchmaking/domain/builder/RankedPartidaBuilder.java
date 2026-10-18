package com.killerprotocol.matchmaking.domain.builder;

import com.killerprotocol.matchmaking.domain.model.*;
import java.time.Instant;

public final class RankedPartidaBuilder implements PartidaBuilder {
    private PartidaId id;
    private String map;
    private String region;
    private String serverAddress;
    private Team teamA;
    private Team teamB;
    private Instant createdAt;
    @Override public PartidaBuilder id(PartidaId value) { id = value; return this; }
    @Override public PartidaBuilder mode(GameMode value) { if (value != GameMode.RANKED) throw new IllegalArgumentException("El builder ranked requiere modo RANKED."); return this; }
    @Override public PartidaBuilder map(String value) { map = value; return this; }
    @Override public PartidaBuilder region(String value) { region = value; return this; }
    @Override public PartidaBuilder serverAddress(String value) { serverAddress = value; return this; }
    @Override public PartidaBuilder teams(Team a, Team b) { teamA = a; teamB = b; return this; }
    @Override public PartidaBuilder createdAt(Instant value) { createdAt = value; return this; }
    @Override public Partida build() { return new Partida(id, GameMode.RANKED, map, region, serverAddress, teamA, teamB, createdAt); }
}
