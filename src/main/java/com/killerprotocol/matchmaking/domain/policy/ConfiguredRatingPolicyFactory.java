package com.killerprotocol.matchmaking.domain.policy;

import com.killerprotocol.matchmaking.domain.model.GameMode;

public final class ConfiguredRatingPolicyFactory implements RatingPolicyCreator {
    private final PolicySettings settings;
    public ConfiguredRatingPolicyFactory(PolicySettings settings) { this.settings = settings; }
    @Override public RatingPolicy create(GameMode mode) { return RatingPolicyFactory.create(mode, settings); }
}
