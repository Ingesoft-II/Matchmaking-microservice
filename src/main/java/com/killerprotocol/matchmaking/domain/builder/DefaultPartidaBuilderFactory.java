package com.killerprotocol.matchmaking.domain.builder;

import com.killerprotocol.matchmaking.domain.model.GameMode;

public class DefaultPartidaBuilderFactory implements PartidaBuilderFactory {
    @Override
    public PartidaBuilder create(GameMode mode) {
        return switch (mode) {
            case RANKED -> new RankedPartidaBuilder();
            case ARCADE -> new ArcadePartidaBuilder();
        };
    }
}
