
package com.killerprotocol.matchmaking.adapter.out.progress;

import com.killerprotocol.matchmaking.domain.model.PlayerId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CachedRatingProviderTest {

    private CountingRatingProvider delegate;
    private CachedRatingProvider cachedProvider;

    private PlayerId player1;
    private PlayerId player2;

    @BeforeEach
    void setUp() {
        delegate = new CountingRatingProvider();
        cachedProvider = new CachedRatingProvider(delegate, 60);

        player1 = new PlayerId("player-1");
        player2 = new PlayerId("player-2");
    }

    @Test
    void getsRatingOnFirstTry() {
        int rating = cachedProvider.ratingOf(player1);

        assertThat(rating).isEqualTo(1001);
        assertThat(delegate.calls()).isEqualTo(1);
    }

    @Test
    void returnsCachedRatingOnMultipleQueries() {
        int first = cachedProvider.ratingOf(player1);
        int second = cachedProvider.ratingOf(player1);

        assertThat(first).isEqualTo(second);
        assertThat(delegate.calls()).isEqualTo(1);
    }

    @Test
    void keepsDifferentEntriesForEachPlayer() {
        int first = cachedProvider.ratingOf(player1);
        int second = cachedProvider.ratingOf(player2);

        assertThat(first).isEqualTo(1001);
        assertThat(second).isEqualTo(1002);
        assertThat(delegate.calls()).isEqualTo(2);
    }

    @Test
    void checkWhenEntryExpires()
            throws InterruptedException {
        CachedRatingProvider shortLivedCache =
                new CachedRatingProvider(delegate, 1);

        int first = shortLivedCache.ratingOf(player1);

        Thread.sleep(1200);

        int second = shortLivedCache.ratingOf(player1);

        assertThat(first).isEqualTo(1001);
        assertThat(second).isEqualTo(1002);
        assertThat(delegate.calls()).isEqualTo(2);
    }

    @Test
    void propagatesErrorWhenRatingNotAvailableNorCache()
    {
        delegate.setFail(true);

        assertThatThrownBy(() -> cachedProvider.ratingOf(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Progreso unavailable");

        assertThat(delegate.calls()).isEqualTo(1);
    }

    @Test
    void returnsLastKnownRatingWhenProviderFailsAfterExpiration()
            throws InterruptedException {

        CachedRatingProvider shortLivedCache =
                new CachedRatingProvider(delegate, 1);

        // First request succeeds and caches the rating.
        int first = shortLivedCache.ratingOf(player1);
        assertThat(first).isEqualTo(1001);

        // Let the cached entry expire.
        Thread.sleep(1200);

        delegate.setFail(true);

        int fallback = shortLivedCache.ratingOf(player1);

        assertThat(fallback).isEqualTo(first);
        assertThat(delegate.calls()).isEqualTo(2);
    }

    private static class CountingRatingProvider
            extends InMemoryRatingProvider {

        private final AtomicInteger callCount = new AtomicInteger();
        private volatile boolean fail;

        CountingRatingProvider() {
            super(1000);
        }

        @Override
        public int ratingOf(PlayerId playerId) {
            callCount.incrementAndGet();

            if (fail) {
                throw new IllegalStateException("Progreso unavailable");
            }

            return 1000 + callCount.get();
        }

        int calls() {
            return callCount.get();
        }

        void setFail(boolean fail) {
            this.fail = fail;
        }
    }
}
