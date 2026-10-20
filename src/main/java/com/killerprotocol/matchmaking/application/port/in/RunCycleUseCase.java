package com.killerprotocol.matchmaking.application.port.in;

import com.killerprotocol.matchmaking.domain.model.Partida;
import java.util.List;

public interface RunCycleUseCase {
    List<Partida> runCycle();
}
