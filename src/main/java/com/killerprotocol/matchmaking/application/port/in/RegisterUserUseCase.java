package com.killerprotocol.matchmaking.application.port.in;

import com.killerprotocol.matchmaking.domain.model.PlayerId;
import com.killerprotocol.matchmaking.domain.model.Role;
import com.killerprotocol.matchmaking.domain.model.UserAccount;

public interface RegisterUserUseCase {
    UserAccount register(PlayerId playerId, String email, String rawPassword, Role role);
}
