package com.killerprotocol.matchmaking.application.port.out;

import com.killerprotocol.matchmaking.domain.model.GameMode;
import com.killerprotocol.matchmaking.domain.model.Ticket;
import com.killerprotocol.matchmaking.domain.model.TicketId;
import java.util.List;
import java.util.Optional;

public interface MatchmakingStore {
    void save(Ticket ticket);
    List<Ticket> findWaiting(GameMode mode);
    List<Ticket> findWaiting(GameMode mode, String region);
    Optional<Ticket> findById(TicketId id);
}
