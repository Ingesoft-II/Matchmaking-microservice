package com.killerprotocol.matchmaking.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TicketTest {

    private static final Instant CREACION = Instant.parse("2026-10-16T22:10:00Z");

    private Ticket ticket() {
        Party party = new Party(new PartyId("pa-1"),
                List.of(new PlayerId("p-1"), new PlayerId("p-2")));
        return new Ticket(new TicketId("t-9f1"), party, GameMode.RANKED, "sa-east", 38, CREACION);
    }

    @Test
    @DisplayName("Un ticket nuevo nace en Waiting sin rating ni partida")
    void naceEnWaitingSinRatingNiPartida() {
        Ticket t = ticket();
        assertThat(t.estado().name()).isEqualTo("WAITING");
        assertThat(t.rating()).isNull();
        assertThat(t.partidaId()).isNull();
        assertThat(t.version()).isZero();
    }

    @Test
    @DisplayName("waitedSec calcula los segundos transcurridos desde createdAt")
    void waitedSecCalculaLosSegundosEsperados() {
        Ticket t = ticket();
        assertThat(t.waitedSec(CREACION)).isZero();
        assertThat(t.waitedSec(CREACION.plusSeconds(45))).isEqualTo(45);
        assertThat(t.waitedSec(CREACION.plusSeconds(3600))).isEqualTo(3600);
    }

    @Test
    @DisplayName("asignarRating fija el promedio del party; el rating no se modifica en ningún otro método")
    void asignarRatingFijaElPromedioDelParty() {
        Ticket t = ticket();
        t.asignarRating(1505);
        assertThat(t.rating()).isEqualTo(1505);
    }

    @Test
    @DisplayName("emparejar fija la partidaId tras transicionar a Matched")
    void emparejarFijaLaPartidaId() {
        Ticket t = ticket();
        t.emparejar(new PartidaId("m-77"));
        assertThat(t.partidaId()).isEqualTo(new PartidaId("m-77"));
        assertThat(t.estado().name()).isEqualTo("MATCHED");
    }

    @Test
    @DisplayName("Los campos inmutables del ticket se conservan intactos")
    void losCamposInmutablesSeConservan() {
        Ticket t = ticket();
        t.cancelar();
        assertThat(t.id()).isEqualTo(new TicketId("t-9f1"));
        assertThat(t.mode()).isEqualTo(GameMode.RANKED);
        assertThat(t.region()).isEqualTo("sa-east");
        assertThat(t.latencyMs()).isEqualTo(38);
        assertThat(t.createdAt()).isEqualTo(CREACION);
        assertThat(t.party().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("La construcción valida los campos obligatorios")
    void laConstruccionValidaLosCamposObligatorios() {
        Party party = new Party(new PartyId("pa-1"), List.of(new PlayerId("p-1")));
        assertThatThrownBy(() -> new Ticket(null, party, GameMode.RANKED, "sa-east", 38, CREACION))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Ticket(new TicketId("t-1"), null, GameMode.RANKED, "sa-east", 38, CREACION))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Ticket(new TicketId("t-1"), party, null, "sa-east", 38, CREACION))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Ticket(new TicketId("t-1"), party, GameMode.RANKED, "  ", 38, CREACION))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Ticket(new TicketId("t-1"), party, GameMode.RANKED, "sa-east", -1, CREACION))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Ticket(new TicketId("t-1"), party, GameMode.RANKED, "sa-east", 38, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("emparejar sin partidaId es inválido")
    void emparejarSinPartidaIdEsInvalido() {
        Ticket t = ticket();
        assertThatThrownBy(() -> t.emparejar(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
