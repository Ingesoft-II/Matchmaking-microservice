package com.killerprotocol.matchmaking.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.killerprotocol.matchmaking.adapter.out.memory.InMemoryMatchmakingStore;
import com.killerprotocol.matchmaking.application.service.ReconnectService;
import com.killerprotocol.matchmaking.domain.model.*;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReconnectServiceTest {
    private final InMemoryMatchmakingStore store = new InMemoryMatchmakingStore();
    private final ReconnectService service = new ReconnectService(store);

    @Test
    void retornaPartidaSiElJugadorEstaEnEquipoA() {
        PlayerId p1 = new PlayerId("p-1");
        List<PlayerId> teamAPlayers = List.of(p1, new PlayerId("p-2"), new PlayerId("p-3"), new PlayerId("p-4"), new PlayerId("p-5"));
        List<PlayerId> teamBPlayers = List.of(new PlayerId("p-6"), new PlayerId("p-7"), new PlayerId("p-8"), new PlayerId("p-9"), new PlayerId("p-10"));

        Partida partida = new Partida(
                new PartidaId("m-1"),
                GameMode.RANKED,
                "Bunker",
                "sa-east",
                "server-1",
                new Team(teamAPlayers),
                new Team(teamBPlayers),
                Instant.now()
        );
        store.saveMatch(partida);

        var result = service.reconnect(p1);

        assertThat(result).isPresent().contains(partida);
    }

    @Test
    void retornaVacioSiElJugadorNoTienePartidaActiva() {
        var result = service.reconnect(new PlayerId("non-existent"));

        assertThat(result).isEmpty();
    }

    @Test
    void lanzaExcepcionSiJugadorEsNulo() {
        assertThatThrownBy(() -> service.reconnect(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
