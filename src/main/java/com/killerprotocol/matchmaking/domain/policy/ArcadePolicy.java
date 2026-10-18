package com.killerprotocol.matchmaking.domain.policy;

import com.killerprotocol.matchmaking.domain.model.Ticket;

public class ArcadePolicy implements RatingPolicy {

    private final int maxLatencyMs;

    public ArcadePolicy(int maxLatencyMs) {
        if (maxLatencyMs < 0) {
            throw new IllegalArgumentException("maxLatencyMs no puede ser negativo.");
        }
        this.maxLatencyMs = maxLatencyMs;
    }

    @Override
    public boolean canMatch(Ticket a, Ticket b, long waitedSec) {
        if (a == null || b == null) {
            return false;
        }
        return a.latencyMs() <= maxLatencyMs && b.latencyMs() <= maxLatencyMs;
    }
}
