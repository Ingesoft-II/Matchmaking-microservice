package com.killerprotocol.matchmaking.domain.builder;

import com.killerprotocol.matchmaking.domain.model.GameMode;

@FunctionalInterface
public interface PartidaBuilderFactory {
    PartidaBuilder create(GameMode mode);
}
