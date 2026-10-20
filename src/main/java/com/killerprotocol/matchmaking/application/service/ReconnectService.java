package com.killerprotocol.matchmaking.application.service;

import com.killerprotocol.matchmaking.application.port.in.ReconnectUseCase;
import com.killerprotocol.matchmaking.application.port.out.MatchmakingStore;
import com.killerprotocol.matchmaking.domain.model.Partida;
import com.killerprotocol.matchmaking.domain.model.PlayerId;
import java.util.Optional;

public class ReconnectService implements ReconnectUseCase {
    private final MatchmakingStore store;

    public ReconnectService(MatchmakingStore store) {
        this.store = store;
    }

    @Override
    public Optional<Partida> reconnect(PlayerId playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo.");
        }
        return store.findActiveMatch(playerId);
    }
}
