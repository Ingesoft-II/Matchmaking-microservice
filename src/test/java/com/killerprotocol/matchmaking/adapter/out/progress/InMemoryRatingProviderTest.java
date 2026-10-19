
package com.killerprotocol.matchmaking.adapter.out.progress;

import com.killerprotocol.matchmaking.domain.model.PlayerId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRatingProviderTest {

    @Test
    void returnsPredeterminedRating() {
        InMemoryRatingProvider provider =
                new InMemoryRatingProvider(1200);

        int rating = provider.ratingOf(
                new PlayerId("player-1")
        );

        assertThat(rating).isEqualTo(1200);
    }

    @Test
    void returnsPredeterminedRatingForDifferentPlayers() {
        InMemoryRatingProvider provider =
                new InMemoryRatingProvider(1000);

        int first = provider.ratingOf(
                new PlayerId("player-1")
        );
        int second = provider.ratingOf(
                new PlayerId("player-2")
        );

        assertThat(first).isEqualTo(1000);
        assertThat(second).isEqualTo(1000);
    }
}
