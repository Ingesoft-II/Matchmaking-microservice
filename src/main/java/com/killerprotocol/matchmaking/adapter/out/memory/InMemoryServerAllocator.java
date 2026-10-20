package com.killerprotocol.matchmaking.adapter.out.memory;

import com.killerprotocol.matchmaking.application.port.out.ServerAllocator;
import com.killerprotocol.matchmaking.domain.model.GameMode;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class InMemoryServerAllocator implements ServerAllocator {
    @Override public String allocate(GameMode mode, String region) {
        return "memory://" + region + "/" + mode.name().toLowerCase() + "/" + UUID.randomUUID();
    }
}
