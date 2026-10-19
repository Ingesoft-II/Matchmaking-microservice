package com.killerprotocol.matchmaking.adapter.out.memory;

import com.killerprotocol.matchmaking.application.port.out.MatchmakingStore;
import com.killerprotocol.matchmaking.domain.model.GameMode;
import com.killerprotocol.matchmaking.domain.model.Ticket;
import com.killerprotocol.matchmaking.domain.model.TicketId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryMatchmakingStore implements MatchmakingStore {
    private final ConcurrentMap<TicketId, Ticket> tickets = new ConcurrentHashMap<>();
    @Override public void save(Ticket ticket) { tickets.put(ticket.id(), ticket); }
    @Override public List<Ticket> findWaiting(GameMode mode) { return findWaiting(mode, null); }
    @Override public List<Ticket> findWaiting(GameMode mode, String region) {
        return tickets.values().stream()
                .filter(t -> t.mode() == mode && (region == null || t.region().equals(region)) && t.estado().name().equals("WAITING"))
                .sorted(Comparator.comparing(Ticket::createdAt).thenComparing(t -> t.id().value()))
                .toList();
    }
    @Override public Optional<Ticket> findById(TicketId id) { return Optional.ofNullable(tickets.get(id)); }
}
