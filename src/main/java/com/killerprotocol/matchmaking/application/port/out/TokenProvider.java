package com.killerprotocol.matchmaking.application.port.out;

import com.killerprotocol.matchmaking.application.port.in.LoginUseCase.AuthToken;
import com.killerprotocol.matchmaking.domain.model.PlayerId;
import com.killerprotocol.matchmaking.domain.model.Role;
import com.killerprotocol.matchmaking.domain.model.UserAccount;
import java.util.Optional;

public interface TokenProvider {

    record TokenClaims(PlayerId playerId, String email, Role role) { }

    AuthToken generateToken(UserAccount account);
    Optional<TokenClaims> validateAndExtract(String token);
}
