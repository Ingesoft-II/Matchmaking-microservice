package com.killerprotocol.matchmaking.domain.policy;

import com.killerprotocol.matchmaking.domain.model.GameMode;

public final class FixedRatingPolicyFactory implements RatingPolicyCreator {
    private final PolicySettings settings;
    public FixedRatingPolicyFactory(PolicySettings settings) { this.settings = settings; }
    @Override public RatingPolicy create(GameMode mode) { return RatingPolicyFactory.create(mode, settings); }
}
