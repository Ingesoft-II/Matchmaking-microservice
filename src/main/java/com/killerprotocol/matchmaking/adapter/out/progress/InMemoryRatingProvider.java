package com.killerprotocol.matchmaking.adapter.out.progress;

import com.killerprotocol.matchmaking.application.port.out.RatingProvider;
import com.killerprotocol.matchmaking.domain.model.PlayerId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRatingProvider implements RatingProvider {
    private final int defaultRating;
    public InMemoryRatingProvider(@Value("${matchmaking.rating.default:1000}") int defaultRating) {
        this.defaultRating = defaultRating;
    }
    @Override public int ratingOf(PlayerId playerId) { return defaultRating; }
}
