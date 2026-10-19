package com.killerprotocol.matchmaking.application.service;

import com.killerprotocol.matchmaking.domain.model.PlayerId;
import com.killerprotocol.matchmaking.domain.model.Team;
import com.killerprotocol.matchmaking.domain.model.Ticket;
import com.killerprotocol.matchmaking.domain.policy.RatingPolicy;
import java.time.Instant;
import java.util.*;

public class MatchBuilder {

    public record MatchGroup(List<Ticket> tickets, Team teamA, Team teamB) { }

    public Optional<MatchGroup> buildMatch(List<Ticket> candidates, RatingPolicy policy, Instant now) {
        if (policy == null) {
            throw new IllegalStateException("No hay una política registrada para el modo solicitado.");
        }
        List<Ticket> group = search(candidates, policy, now, 0, new ArrayList<>(), 0);
        if (group == null) {
            return Optional.empty();
        }
        List<List<PlayerId>> teams = partition(group);
        if (teams == null) {
            return Optional.empty();
        }
        return Optional.of(new MatchGroup(group, new Team(teams.get(0)), new Team(teams.get(1))));
    }

    private List<Ticket> search(List<Ticket> candidates, RatingPolicy policy, Instant now,
                                int index, List<Ticket> selected, int players) {
        if (players == 10) {
            return partition(selected) == null ? null : List.copyOf(selected);
        }
        if (players > 10 || index >= candidates.size()) {
            return null;
        }
        for (int i = index; i < candidates.size(); i++) {
            Ticket candidate = candidates.get(i);
            int size = candidate.party().size();
            if (players + size > 10 || !compatible(candidate, selected, policy, now)) {
                continue;
            }
            selected.add(candidate);
            List<Ticket> found = search(candidates, policy, now, i + 1, selected, players + size);
            selected.remove(selected.size() - 1);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private boolean compatible(Ticket candidate, List<Ticket> selected, RatingPolicy policy, Instant now) {
        for (Ticket other : selected) {
            long waited = Math.min(candidate.waitedSec(now), other.waitedSec(now));
            if (!policy.canMatch(candidate, other, waited)) {
                return false;
            }
        }
        return true;
    }

    private List<List<PlayerId>> partition(List<Ticket> tickets) {
        Set<PlayerId> all = new HashSet<>();
        for (Ticket ticket : tickets) {
            for (PlayerId id : ticket.party().members()) {
                if (!all.add(id)) {
                    return null;
                }
            }
        }
        List<Ticket> ordered = tickets.stream()
                .sorted(Comparator.comparingInt((Ticket t) -> t.party().size()).reversed())
                .toList();
        List<PlayerId> a = new ArrayList<>();
        List<PlayerId> b = new ArrayList<>();
        return place(ordered, 0, a, b) ? List.of(List.copyOf(a), List.copyOf(b)) : null;
    }

    private boolean place(List<Ticket> tickets, int index, List<PlayerId> a, List<PlayerId> b) {
        if (index == tickets.size()) {
            return a.size() == 5 && b.size() == 5;
        }
        List<PlayerId> members = tickets.get(index).party().members();
        if (a.size() + members.size() <= 5) {
            a.addAll(members);
            if (place(tickets, index + 1, a, b)) {
                return true;
            }
            a.subList(a.size() - members.size(), a.size()).clear();
        }
        if (b.size() + members.size() <= 5) {
            b.addAll(members);
            if (place(tickets, index + 1, a, b)) {
                return true;
            }
            b.subList(b.size() - members.size(), b.size()).clear();
        }
        return false;
    }
}
