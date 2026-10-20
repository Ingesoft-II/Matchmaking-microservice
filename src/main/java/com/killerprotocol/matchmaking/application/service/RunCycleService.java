package com.killerprotocol.matchmaking.application.service;

import com.killerprotocol.matchmaking.application.port.in.RunCycleUseCase;
import com.killerprotocol.matchmaking.application.port.out.EventPublisher;
import com.killerprotocol.matchmaking.application.port.out.MatchmakingStore;
import com.killerprotocol.matchmaking.application.port.out.ServerAllocator;
import com.killerprotocol.matchmaking.domain.builder.DefaultPartidaBuilderFactory;
import com.killerprotocol.matchmaking.domain.builder.PartidaBuilder;
import com.killerprotocol.matchmaking.domain.builder.PartidaBuilderFactory;
import com.killerprotocol.matchmaking.domain.model.*;
import com.killerprotocol.matchmaking.domain.policy.RatingPolicies;
import java.time.Instant;
import java.util.*;

public class RunCycleService implements RunCycleUseCase {
    private final MatchmakingStore store;
    private final ServerAllocator servers;
    private final EventPublisher events;
    private final RatingPolicies policies;
    private final MatchBuilder matchBuilder;
    private final PartidaBuilderFactory builderFactory;
    private final List<String> rankedMaps;
    private final List<String> arcadeMaps;

    public RunCycleService(MatchmakingStore store, ServerAllocator servers, EventPublisher events,
                           RatingPolicies policies, MatchBuilder matchBuilder,
                           PartidaBuilderFactory builderFactory,
                           List<String> rankedMaps, List<String> arcadeMaps) {
        this.store = store;
        this.servers = servers;
        this.events = events;
        this.policies = policies;
        this.matchBuilder = matchBuilder;
        this.builderFactory = builderFactory;
        this.rankedMaps = List.copyOf(rankedMaps);
        this.arcadeMaps = List.copyOf(arcadeMaps);
    }

    public RunCycleService(MatchmakingStore store, ServerAllocator servers, EventPublisher events,
                           RatingPolicies policies,
                           List<String> rankedMaps, List<String> arcadeMaps) {
        this(store, servers, events, policies, new MatchBuilder(), new DefaultPartidaBuilderFactory(), rankedMaps, arcadeMaps);
    }

    @Override
    public List<Partida> runCycle() {
        List<Partida> created = new ArrayList<>();
        Instant now = Instant.now();
        for (GameMode mode : GameMode.values()) {
            Set<String> regions = new LinkedHashSet<>();
            store.findWaiting(mode).forEach(ticket -> regions.add(ticket.region()));
            for (String region : regions) {
                while (true) {
                    List<Ticket> waiting = store.findWaiting(mode, region);
                    Optional<MatchBuilder.MatchGroup> group = matchBuilder.buildMatch(waiting, policies.forMode(mode), now);
                    if (group.isEmpty()) {
                        break;
                    }
                    MatchBuilder.MatchGroup match = group.get();
                    String map = chooseMap(mode, created.size());
                    String server = servers.allocate(mode, region);
                    PartidaBuilder builder = builderFactory.create(mode);
                    Partida partida = builder.id(new PartidaId(UUID.randomUUID().toString()))
                            .mode(mode)
                            .map(map)
                            .region(region)
                            .serverAddress(server)
                            .teams(match.teamA(), match.teamB())
                            .createdAt(now)
                            .build();

                    match.tickets().forEach(ticket -> {
                        ticket.emparejar(partida.id());
                        store.save(ticket);
                    });
                    store.saveMatch(partida);
                    events.partidaCreada(partida);
                    created.add(partida);
                }
            }
        }
        return List.copyOf(created);
    }

    private String chooseMap(GameMode mode, int index) {
        List<String> maps = mode == GameMode.RANKED ? rankedMaps : arcadeMaps;
        if (maps.isEmpty()) {
            throw new IllegalStateException("Debe existir al menos un mapa configurado para " + mode);
        }
        return maps.get(index % maps.size());
    }
}
