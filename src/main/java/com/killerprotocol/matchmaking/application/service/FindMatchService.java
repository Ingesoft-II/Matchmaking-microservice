package com.killerprotocol.matchmaking.application.service;

import com.killerprotocol.matchmaking.application.port.in.FindMatchUseCase;
import com.killerprotocol.matchmaking.application.port.out.MatchmakingStore;
import com.killerprotocol.matchmaking.application.port.out.RatingProvider;
import com.killerprotocol.matchmaking.domain.model.*;
import java.time.Instant;
import java.util.UUID;

public class FindMatchService implements FindMatchUseCase {
    private final MatchmakingStore store;
    private final RatingProvider ratings;
    public FindMatchService(MatchmakingStore store, RatingProvider ratings) {
        this.store = store;
        this.ratings = ratings;
    }
    @Override public Ticket findMatch(Party party, GameMode mode, String region, int latencyMs) {
        Ticket ticket = new Ticket(new TicketId(UUID.randomUUID().toString()), party, mode,
                region, latencyMs, Instant.now());
        if (mode == GameMode.RANKED) {
            int rating = (int) Math.round(party.members().stream().mapToInt(ratings::ratingOf).average().orElse(1000));
            ticket.asignarRating(rating);
        }
        store.save(ticket);
        return ticket;
    }
}
