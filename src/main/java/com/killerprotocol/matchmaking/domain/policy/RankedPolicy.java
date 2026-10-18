package com.killerprotocol.matchmaking.domain.policy;

import com.killerprotocol.matchmaking.domain.model.Ticket;

public class RankedPolicy implements RatingPolicy {

    private final int baseGap;
    private final double growthPerSec;

    public RankedPolicy(int baseGap, double growthPerSec) {
        if (baseGap < 0) {
            throw new IllegalArgumentException("baseGap no puede ser negativo.");
        }
        if (growthPerSec < 0) {
            throw new IllegalArgumentException("growthPerSec no puede ser negativo.");
        }
        this.baseGap = baseGap;
        this.growthPerSec = growthPerSec;
    }

    @Override
    public boolean canMatch(Ticket a, Ticket b, long waitedSec) {
        if (a == null || b == null) {
            return false;
        }

        if (a.rating() == null || b.rating() == null) {
            return false;
        }
        return Math.abs(a.rating() - b.rating()) <= margen(waitedSec);
    }

    private double margen(long waitedSec) {
        return baseGap + growthPerSec * Math.max(0L, waitedSec);
    }
}
