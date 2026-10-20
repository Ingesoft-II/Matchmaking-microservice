package com.killerprotocol.matchmaking.adapter.out.progress;

import com.killerprotocol.matchmaking.application.port.out.RatingProvider;
import com.killerprotocol.matchmaking.domain.model.PlayerId;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class CachedRatingProvider implements RatingProvider {
    private record Entry(int rating, Instant expiresAt) { }
    private final RatingProvider delegate;
    private final Duration ttl;
    private final ConcurrentMap<PlayerId, Entry> cache = new ConcurrentHashMap<>();

    public CachedRatingProvider(@Qualifier("inMemoryRatingProvider") RatingProvider delegate,
                                @Value("${matchmaking.rating.cache-ttl-sec:60}") long ttlSeconds) {
        this.delegate = delegate;
        this.ttl = Duration.ofSeconds(Math.max(0, ttlSeconds));
    }

    @Override
    public int ratingOf(PlayerId playerId) {
        Instant now = Instant.now();
        Entry entry = cache.get(playerId);
        if (entry != null && now.isBefore(entry.expiresAt())) {
            return entry.rating();
        }
        try {
            int rating = delegate.ratingOf(playerId);
            cache.put(playerId, new Entry(rating, now.plus(ttl)));
            return rating;
        } catch (Exception e) {
            if (entry != null) {
                return entry.rating();
            }
            throw e;
        }
    }
}
