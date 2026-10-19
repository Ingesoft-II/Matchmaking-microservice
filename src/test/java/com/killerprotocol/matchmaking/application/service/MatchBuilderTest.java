package com.killerprotocol.matchmaking.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.killerprotocol.matchmaking.domain.model.*;
import com.killerprotocol.matchmaking.domain.policy.RatingPolicy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MatchBuilderTest {
    private static final Instant NOW = Instant.parse("2026-10-19T20:00:00Z");
    private final MatchBuilder matchBuilder = new MatchBuilder();
    private final RatingPolicy allowAllPolicy = (a, b, waited) -> true;

    @Test
    void armaDosEquiposDeCincoConDiezTicketsIndividuales() {
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            PlayerId player = new PlayerId("p-" + i);
            Party party = new Party(new PartyId("party-" + i), List.of(player));
            tickets.add(new Ticket(new TicketId("t-" + i), party, GameMode.RANKED, "sa-east", 30, NOW));
        }

        var result = matchBuilder.buildMatch(tickets, allowAllPolicy, NOW);

        assertThat(result).isPresent();
        assertThat(result.get().teamA().players()).hasSize(5);
        assertThat(result.get().teamB().players()).hasSize(5);
        assertThat(result.get().tickets()).hasSize(10);
    }

    @Test
    void noArmaPartidaSiHayMenosDeDiezJugadores() {
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            PlayerId player = new PlayerId("p-" + i);
            Party party = new Party(new PartyId("party-" + i), List.of(player));
            tickets.add(new Ticket(new TicketId("t-" + i), party, GameMode.RANKED, "sa-east", 30, NOW));
        }

        var result = matchBuilder.buildMatch(tickets, allowAllPolicy, NOW);

        assertThat(result).isEmpty();
    }
}
