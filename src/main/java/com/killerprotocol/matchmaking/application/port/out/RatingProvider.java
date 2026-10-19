package com.killerprotocol.matchmaking.application.port.out;

import com.killerprotocol.matchmaking.domain.model.PlayerId;

public interface RatingProvider {
    int ratingOf(PlayerId playerId);
}
