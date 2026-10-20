package com.killerprotocol.matchmaking.application.port.out;

import com.killerprotocol.matchmaking.domain.model.Partida;

public interface EventPublisher {
    void partidaCreada(Partida partida);
}
