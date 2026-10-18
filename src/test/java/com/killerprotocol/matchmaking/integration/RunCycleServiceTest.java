package com.killerprotocol.matchmaking.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.killerprotocol.matchmaking.adapter.out.memory.InMemoryEventPublisher;
import com.killerprotocol.matchmaking.adapter.out.memory.InMemoryMatchmakingStore;
import com.killerprotocol.matchmaking.application.port.out.ServerAllocator;
import com.killerprotocol.matchmaking.application.service.RunCycleService;
import com.killerprotocol.matchmaking.domain.model.*;
import com.killerprotocol.matchmaking.domain.policy.RatingPolicies;
import com.killerprotocol.matchmaking.domain.policy.RatingPolicy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class RunCycleServiceTest {
    private static final Instant CREATED = Instant.parse("2026-10-16T22:10:00Z");

    @Test
    void formaDosEquiposDeCincoCuandoHayDiezJugadoresCompatibles() {
        InMemoryMatchmakingStore store = new InMemoryMatchmakingStore();
        addTickets(store, 10, GameMode.RANKED);
        var partidas = service(store, (a, b, waited) -> true).runCycle();
        assertThat(partidas).hasSize(1);
        assertThat(partidas.getFirst().teamA().players()).hasSize(5);
        assertThat(partidas.getFirst().teamB().players()).hasSize(5);
        assertThat(partidas.getFirst().serverAddress()).startsWith("memory://");
        assertThat(store.findWaiting(GameMode.RANKED)).isEmpty();
    }

    @Test
    void noCreaPartidaConMenosDeDiezJugadores() {
        InMemoryMatchmakingStore store = new InMemoryMatchmakingStore();
        addTickets(store, 9, GameMode.ARCADE);
        assertThat(service(store, (a, b, waited) -> true).runCycle()).isEmpty();
        assertThat(store.findWaiting(GameMode.ARCADE)).hasSize(9);
    }

    @Test
    void unaNuevaStrategySePuedeRegistrarSinModificarElServicioDeCiclo() {
        InMemoryMatchmakingStore store = new InMemoryMatchmakingStore();
        addTickets(store, 10, GameMode.RANKED);
        AtomicBoolean used = new AtomicBoolean();
        RatingPolicy seasonalPolicy = (a, b, waited) -> { used.set(true); return true; };
        assertThat(service(store, seasonalPolicy).runCycle()).hasSize(1);
        assertThat(used).isTrue();
    }

    private RunCycleService service(InMemoryMatchmakingStore store, RatingPolicy ranked) {
        RatingPolicy arcade = (a, b, waited) -> true;
        ServerAllocator allocator = (mode, region) -> "memory://" + region + "/test";
        return new RunCycleService(store, allocator, new InMemoryEventPublisher(),
                new RatingPolicies(Map.of(GameMode.RANKED, ranked, GameMode.ARCADE, arcade)),
                List.of("Bunker"), List.of("Arena"));
    }

    private void addTickets(InMemoryMatchmakingStore store, int count, GameMode mode) {
        for (int i = 0; i < count; i++) {
            PlayerId player = new PlayerId("player-" + mode + "-" + i);
            Party party = new Party(new PartyId("party-" + mode + "-" + i), List.of(player));
            Ticket ticket = new Ticket(new TicketId("ticket-" + mode + "-" + i), party, mode,
                    "sa-east", 40, CREATED);
            ticket.asignarRating(i * 100);
            store.save(ticket);
        }
    }
}
