package com.killerprotocol.matchmaking.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PartyTest {

    private static PartyId id() {
        return new PartyId("pa-1");
    }

    private static PlayerId jugador(String... ids) {
        return new PlayerId(ids[0]);
    }

    @Test
    @DisplayName("Un party de 1 jugador es válido")
    void unJugadorEsValido() {
        Party p = new Party(id(), List.of(jugador("p-1")));
        assertThat(p.size()).isEqualTo(1);
        assertThat(p.members()).containsExactly(jugador("p-1"));
    }

    @Test
    @DisplayName("Un party de 5 jugadores es válido (tamaño máximo)")
    void cincoJugadoresEsValido() {
        Party p = new Party(id(), List.of(jugador("p-1"), jugador("p-2"), jugador("p-3"),
                jugador("p-4"), jugador("p-5")));
        assertThat(p.size()).isEqualTo(5);
    }

    @Test
    @DisplayName("Un party sin jugadores es inválido")
    void ceroJugadoresEsInvalido() {
        assertThatThrownBy(() -> new Party(id(), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Party(id(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Un party de 6 jugadores es inválido")
    void seisJugadoresEsInvalido() {
        List<PlayerId> seis = List.of(jugador("p-1"), jugador("p-2"), jugador("p-3"),
                jugador("p-4"), jugador("p-5"), jugador("p-6"));
        assertThatThrownBy(() -> new Party(id(), seis))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Los jugadores duplicados son inválidos")
    void jugadoresDuplicadosSonInvalidos() {
        List<PlayerId> duplicados = List.of(jugador("p-1"), jugador("p-2"), jugador("p-1"));
        assertThatThrownBy(() -> new Party(id(), duplicados))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("El party no es nulo: id, members, modo y región se conservan")
    void elIdNuloEsInvalido() {
        assertThatThrownBy(() -> new Party(null, List.of(jugador("p-1"))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("La lista de miembros es inmutable: no admite modificaciones")
    void laListaDeMiembrosEsInmutable() {
        List<PlayerId> originales = new java.util.ArrayList<>(List.of(jugador("p-1")));
        Party p = new Party(id(), originales);
        assertThatThrownBy(() -> p.members().add(jugador("p-2")))
                .isInstanceOf(UnsupportedOperationException.class);

        originales.add(jugador("p-9"));
        assertThat(p.size()).isEqualTo(1);
    }
}
