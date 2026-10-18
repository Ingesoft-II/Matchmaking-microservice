package com.killerprotocol.matchmaking.application.service;

import com.killerprotocol.matchmaking.application.port.in.GetTicketUseCase;
import com.killerprotocol.matchmaking.application.port.out.MatchmakingStore;
import com.killerprotocol.matchmaking.domain.model.Ticket;
import com.killerprotocol.matchmaking.domain.model.TicketId;
import java.util.NoSuchElementException;

public class GetTicketService implements GetTicketUseCase {
    private final MatchmakingStore store;
    public GetTicketService(MatchmakingStore store) { this.store = store; }
    @Override public Ticket get(TicketId id) {
        return store.findById(id).orElseThrow(() -> new NoSuchElementException("Ticket no encontrado."));
    }
}
