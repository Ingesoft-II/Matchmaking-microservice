package com.killerprotocol.matchmaking.domain.policy;

public record PolicySettings(RankedSettings ranked, ArcadeSettings arcade) {

    public PolicySettings {
        if (ranked == null) {
            throw new IllegalArgumentException("Faltan los parámetros de la política ranked.");
        }
        if (arcade == null) {
            throw new IllegalArgumentException("Faltan los parámetros de la política arcade.");
        }
    }

    public record RankedSettings(int baseGap, double growthPerSec) {

        public RankedSettings {
            if (baseGap < 0) {
                throw new IllegalArgumentException("baseGap no puede ser negativo.");
            }
            if (growthPerSec < 0) {
                throw new IllegalArgumentException("growthPerSec no puede ser negativo.");
            }
        }
    }

    public record ArcadeSettings(int maxLatencyMs) {

        public ArcadeSettings {
            if (maxLatencyMs < 0) {
                throw new IllegalArgumentException("maxLatencyMs no puede ser negativo.");
            }
        }
    }
}
