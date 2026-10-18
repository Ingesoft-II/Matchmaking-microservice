package com.killerprotocol.matchmaking.domain.policy;

import com.killerprotocol.matchmaking.domain.model.GameMode;

public final class RatingPolicyFactory {

    private RatingPolicyFactory() {
        
    }

    public static RatingPolicy create(GameMode mode, PolicySettings settings) {
        if (mode == null) {
            throw new IllegalArgumentException("El modo de juego no puede ser nulo.");
        }
        if (settings == null) {
            throw new IllegalArgumentException("Los parámetros de política no pueden ser nulos.");
        }
        return switch (mode) {
            case RANKED -> new RankedPolicy(settings.ranked().baseGap(), settings.ranked().growthPerSec());
            case ARCADE -> new ArcadePolicy(settings.arcade().maxLatencyMs());
        };
    }
}
