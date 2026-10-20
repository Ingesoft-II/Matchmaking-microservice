package com.killerprotocol.matchmaking.domain.policy;

import com.killerprotocol.matchmaking.domain.model.GameMode;

public interface RatingPolicyCreator {
    RatingPolicy create(GameMode mode);
}
