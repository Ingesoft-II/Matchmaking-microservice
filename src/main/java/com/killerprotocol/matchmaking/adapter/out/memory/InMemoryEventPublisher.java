package com.killerprotocol.matchmaking.adapter.out.memory;

import com.killerprotocol.matchmaking.application.port.out.EventPublisher;
import com.killerprotocol.matchmaking.domain.model.Partida;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class InMemoryEventPublisher implements EventPublisher {
    private final List<Partida> partidasCreadas = new CopyOnWriteArrayList<>();
    @Override public void partidaCreada(Partida partida) { partidasCreadas.add(partida); }
    public List<Partida> published() { return List.copyOf(partidasCreadas); }
}
