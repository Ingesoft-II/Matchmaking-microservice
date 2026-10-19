package com.killerprotocol.matchmaking.application.port.in;

import com.killerprotocol.matchmaking.domain.model.Partida;
import com.killerprotocol.matchmaking.domain.model.PlayerId;
import java.util.Optional;

public interface ReconnectUseCase {
    Optional<Partida> reconnect(PlayerId playerId);
}
