package com.killerprotocol.matchmaking.domain.policy;

import com.killerprotocol.matchmaking.domain.model.GameMode;
import java.util.Map;

public final class RatingPolicies {
    private final Map<GameMode, RatingPolicy> byMode;
    public RatingPolicies(Map<GameMode, RatingPolicy> byMode) {
        if (byMode == null || byMode.isEmpty() || byMode.values().stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Debe registrarse al menos una política válida.");
        }
        this.byMode = Map.copyOf(byMode);
    }
    public RatingPolicy forMode(GameMode mode) {
        RatingPolicy policy = byMode.get(mode);
        if (policy == null) throw new IllegalArgumentException("No existe política para " + mode);
        return policy;
    }
}
