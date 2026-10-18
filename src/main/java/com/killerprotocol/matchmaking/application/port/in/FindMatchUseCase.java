package com.killerprotocol.matchmaking.application.port.in;

import com.killerprotocol.matchmaking.domain.model.GameMode;
import com.killerprotocol.matchmaking.domain.model.Party;
import com.killerprotocol.matchmaking.domain.model.Ticket;

public interface FindMatchUseCase {
    Ticket findMatch(Party party, GameMode mode, String region, int latencyMs);
}
