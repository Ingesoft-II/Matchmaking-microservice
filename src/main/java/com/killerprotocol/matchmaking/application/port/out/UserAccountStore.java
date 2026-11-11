package com.killerprotocol.matchmaking.application.port.out;

import com.killerprotocol.matchmaking.domain.model.PlayerId;
import com.killerprotocol.matchmaking.domain.model.UserAccount;
import java.util.Optional;

public interface UserAccountStore {
    void save(UserAccount account);
    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findByPlayerId(PlayerId playerId);
}
