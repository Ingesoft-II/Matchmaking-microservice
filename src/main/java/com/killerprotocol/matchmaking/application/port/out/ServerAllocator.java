package com.killerprotocol.matchmaking.application.port.out;

import com.killerprotocol.matchmaking.domain.model.GameMode;

public interface ServerAllocator {
    String allocate(GameMode mode, String region);
}
