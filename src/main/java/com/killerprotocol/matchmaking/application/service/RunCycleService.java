package com.killerprotocol.matchmaking.application.service;

import com.killerprotocol.matchmaking.application.port.in.RunCycleUseCase;
import com.killerprotocol.matchmaking.application.port.out.EventPublisher;
import com.killerprotocol.matchmaking.application.port.out.MatchmakingStore;
import com.killerprotocol.matchmaking.application.port.out.ServerAllocator;
import com.killerprotocol.matchmaking.domain.builder.ArcadePartidaBuilder;
import com.killerprotocol.matchmaking.domain.builder.PartidaBuilder;
import com.killerprotocol.matchmaking.domain.builder.RankedPartidaBuilder;
import com.killerprotocol.matchmaking.domain.model.*;
import com.killerprotocol.matchmaking.domain.policy.RatingPolicy;
import com.killerprotocol.matchmaking.domain.policy.RatingPolicies;
import java.time.Instant;
import java.util.*;

public class RunCycleService implements RunCycleUseCase {
    private final MatchmakingStore store;
    private final ServerAllocator servers;
    private final EventPublisher events;
    private final RatingPolicies policies;
    private final List<String> rankedMaps;
    private final List<String> arcadeMaps;

    public RunCycleService(MatchmakingStore store, ServerAllocator servers, EventPublisher events,
            RatingPolicies policies,
            List<String> rankedMaps, List<String> arcadeMaps) {
        this.store = store;
        this.servers = servers;
        this.events = events;
        this.policies = policies;
        this.rankedMaps = List.copyOf(rankedMaps);
        this.arcadeMaps = List.copyOf(arcadeMaps);
    }

    @Override public List<Partida> runCycle() {
        List<Partida> created = new ArrayList<>();
        Instant now = Instant.now();
        for (GameMode mode : GameMode.values()) {
            Set<String> regions = new LinkedHashSet<>();
            store.findWaiting(mode).forEach(ticket -> regions.add(ticket.region()));
            for (String region : regions) {
                while (true) {
                    List<Ticket> waiting = store.findWaiting(mode, region);
                    List<Ticket> match = findGroup(waiting, policies.forMode(mode), now);
                    if (match == null) break;
                    List<List<PlayerId>> teams = partition(match);
                    if (teams == null) break;
                    Team teamA = new Team(teams.get(0));
                    Team teamB = new Team(teams.get(1));
                    String map = chooseMap(mode, created.size());
                    String server = servers.allocate(mode, region);
                    PartidaBuilder builder = mode == GameMode.RANKED ? new RankedPartidaBuilder() : new ArcadePartidaBuilder();
                    Partida partida = builder.id(new PartidaId(UUID.randomUUID().toString()))
                            .mode(mode).map(map).region(region).serverAddress(server)
                            .teams(teamA, teamB).createdAt(now).build();
                    match.forEach(ticket -> { ticket.emparejar(partida.id()); store.save(ticket); });
                    events.partidaCreada(partida);
                    created.add(partida);
                }
            }
        }
        return List.copyOf(created);
    }

    private String chooseMap(GameMode mode, int index) {
        List<String> maps = mode == GameMode.RANKED ? rankedMaps : arcadeMaps;
        if (maps.isEmpty()) throw new IllegalStateException("Debe existir al menos un mapa configurado para " + mode);
        return maps.get(index % maps.size());
    }

    private List<Ticket> findGroup(List<Ticket> candidates, RatingPolicy policy, Instant now) {
        if (policy == null) throw new IllegalStateException("No hay una política registrada para el modo solicitado.");
        return search(candidates, policy, now, 0, new ArrayList<>(), 0);
    }

    private List<Ticket> search(List<Ticket> candidates, RatingPolicy policy, Instant now,
            int index, List<Ticket> selected, int players) {
        if (players == 10) return partition(selected) == null ? null : List.copyOf(selected);
        if (players > 10 || index >= candidates.size()) return null;
        for (int i = index; i < candidates.size(); i++) {
            Ticket candidate = candidates.get(i);
            int size = candidate.party().size();
            if (players + size > 10 || !compatible(candidate, selected, policy, now)) continue;
            selected.add(candidate);
            List<Ticket> found = search(candidates, policy, now, i + 1, selected, players + size);
            selected.remove(selected.size() - 1);
            if (found != null) return found;
        }
        return null;
    }

    private boolean compatible(Ticket candidate, List<Ticket> selected, RatingPolicy policy, Instant now) {
        for (Ticket other : selected) {
            long waited = Math.min(candidate.waitedSec(now), other.waitedSec(now));
            if (!policy.canMatch(candidate, other, waited)) return false;
        }
        return true;
    }

    private List<List<PlayerId>> partition(List<Ticket> tickets) {
        Set<PlayerId> all = new HashSet<>();
        for (Ticket ticket : tickets) for (PlayerId id : ticket.party().members()) if (!all.add(id)) return null;
        List<Ticket> ordered = tickets.stream().sorted(Comparator.comparingInt((Ticket t) -> t.party().size()).reversed()).toList();
        List<PlayerId> a = new ArrayList<>(), b = new ArrayList<>();
        return place(ordered, 0, a, b) ? List.of(List.copyOf(a), List.copyOf(b)) : null;
    }

    private boolean place(List<Ticket> tickets, int index, List<PlayerId> a, List<PlayerId> b) {
        if (index == tickets.size()) return a.size() == 5 && b.size() == 5;
        List<PlayerId> members = tickets.get(index).party().members();
        if (a.size() + members.size() <= 5) {
            a.addAll(members);
            if (place(tickets, index + 1, a, b)) return true;
            a.subList(a.size() - members.size(), a.size()).clear();
        }
        if (b.size() + members.size() <= 5) {
            b.addAll(members);
            if (place(tickets, index + 1, a, b)) return true;
            b.subList(b.size() - members.size(), b.size()).clear();
        }
        return false;
    }
}
