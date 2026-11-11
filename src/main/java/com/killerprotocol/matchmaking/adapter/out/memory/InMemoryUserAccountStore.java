package com.killerprotocol.matchmaking.adapter.out.memory;

import com.killerprotocol.matchmaking.application.port.out.UserAccountStore;
import com.killerprotocol.matchmaking.domain.model.PlayerId;
import com.killerprotocol.matchmaking.domain.model.UserAccount;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserAccountStore implements UserAccountStore {

    private final ConcurrentMap<PlayerId, UserAccount> byPlayerId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, UserAccount> byEmail = new ConcurrentHashMap<>();

    @Override
    public void save(UserAccount account) {
        byPlayerId.put(account.playerId(), account);
        byEmail.put(account.email().toLowerCase(), account);
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byEmail.get(email.trim().toLowerCase()));
    }

    @Override
    public Optional<UserAccount> findByPlayerId(PlayerId playerId) {
        if (playerId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byPlayerId.get(playerId));
    }
}
