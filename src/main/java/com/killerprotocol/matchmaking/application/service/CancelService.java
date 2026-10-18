package com.killerprotocol.matchmaking.application.service;

import com.killerprotocol.matchmaking.application.port.in.CancelUseCase;
import com.killerprotocol.matchmaking.application.port.out.MatchmakingStore;
import com.killerprotocol.matchmaking.domain.model.Ticket;
import com.killerprotocol.matchmaking.domain.model.TicketId;
import java.util.NoSuchElementException;

public class CancelService implements CancelUseCase {
    private final MatchmakingStore store;
    public CancelService(MatchmakingStore store) { this.store = store; }
    @Override public Ticket cancel(TicketId id) {
        Ticket ticket = store.findById(id).orElseThrow(() -> new NoSuchElementException("Ticket no encontrado."));
        ticket.cancelar();
        store.save(ticket);
        return ticket;
    }
}
