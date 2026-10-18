package com.killerprotocol.matchmaking.domain.state;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.killerprotocol.matchmaking.domain.exception.TransicionInvalidaException;
import com.killerprotocol.matchmaking.domain.model.GameMode;
import com.killerprotocol.matchmaking.domain.model.Party;
import com.killerprotocol.matchmaking.domain.model.PartyId;
import com.killerprotocol.matchmaking.domain.model.PartidaId;
import com.killerprotocol.matchmaking.domain.model.PlayerId;
import com.killerprotocol.matchmaking.domain.model.Ticket;
import com.killerprotocol.matchmaking.domain.model.TicketId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


class TicketStateTransitionsTest {

    private static final Instant CREACION = Instant.parse("2026-10-16T22:10:00Z");
    private static final PartidaId PARTIDA = new PartidaId("m-77");

    private Ticket ticketEsperando() {
        Party party = new Party(new PartyId("pa-1"),
                List.of(new PlayerId("p-1"), new PlayerId("p-2")));

        return new Ticket(new TicketId("t-9f1"), party, GameMode.RANKED, "sa-east", 38, CREACION);
    }

    @Nested
    @DisplayName("Waiting: único estado de partida")
    class DesdeWaiting {

        @Test
        void cancelar_lleva_a_Cancelled() {
            Ticket t = ticketEsperando();
            t.cancelar();
            assertThat(t.estado()).isInstanceOf(Cancelled.class);
            assertThat(t.estado().name()).isEqualTo("CANCELLED");
        }

        @Test
        void emparejar_lleva_a_Matched_y_fija_la_partidaId() {
            Ticket t = ticketEsperando();
            t.emparejar(PARTIDA);
            assertThat(t.estado()).isInstanceOf(Matched.class);
            assertThat(t.estado().name()).isEqualTo("MATCHED");
            assertThat(t.partidaId()).isEqualTo(PARTIDA);
        }

        @Test
        void expirar_lleva_a_Expired() {
            Ticket t = ticketEsperando();
            t.expirar();
            assertThat(t.estado()).isInstanceOf(Expired.class);
            assertThat(t.estado().name()).isEqualTo("EXPIRED");
        }
    }

    @Nested
    @DisplayName("Matched: estado terminal, no admite ninguna operación")
    class DesdeMatched {

        private Ticket ticketEmparejado() {
            Ticket t = ticketEsperando();
            t.emparejar(PARTIDA);
            return t;
        }

        @Test
        void cancelar_lanza_TransicionInvalidaException() {
            Ticket t = ticketEmparejado();
            assertThatThrownBy(t::cancelar)
                    .isInstanceOf(TransicionInvalidaException.class)
                    .hasMessageContaining("MATCHED")
                    .hasMessageContaining("cancelar");
        }

        @Test
        void emparejar_lanza_TransicionInvalidaException() {
            Ticket t = ticketEmparejado();
            assertThatThrownBy(() -> t.emparejar(new PartidaId("m-78")))
                    .isInstanceOf(TransicionInvalidaException.class)
                    .hasMessageContaining("MATCHED")
                    .hasMessageContaining("emparejar");
        }

        @Test
        void expirar_lanza_TransicionInvalidaException() {
            Ticket t = ticketEmparejado();
            assertThatThrownBy(t::expirar)
                    .isInstanceOf(TransicionInvalidaException.class)
                    .hasMessageContaining("MATCHED")
                    .hasMessageContaining("expirar");
        }

        @Test
        void el_estado_y_la_partidaId_se_mantienen_inalterados() {
            Ticket t = ticketEmparejado();
            assertThatThrownBy(t::cancelar).isInstanceOf(TransicionInvalidaException.class);
            assertThat(t.estado()).isInstanceOf(Matched.class);
            assertThat(t.partidaId()).isEqualTo(PARTIDA);
        }
    }

    @Nested
    @DisplayName("Cancelled: estado terminal, no admite ninguna operación")
    class DesdeCancelled {

        private Ticket ticketCancelado() {
            Ticket t = ticketEsperando();
            t.cancelar();
            return t;
        }

        @Test
        void cancelar_lanza_TransicionInvalidaException() {
            Ticket t = ticketCancelado();
            assertThatThrownBy(t::cancelar)
                    .isInstanceOf(TransicionInvalidaException.class)
                    .hasMessageContaining("CANCELLED");
        }

        @Test
        void emparejar_lanza_TransicionInvalidaException() {
            Ticket t = ticketCancelado();
            assertThatThrownBy(() -> t.emparejar(PARTIDA))
                    .isInstanceOf(TransicionInvalidaException.class)
                    .hasMessageContaining("CANCELLED");
        }

        @Test
        void expirar_lanza_TransicionInvalidaException() {
            Ticket t = ticketCancelado();
            assertThatThrownBy(t::expirar)
                    .isInstanceOf(TransicionInvalidaException.class)
                    .hasMessageContaining("CANCELLED");
        }
    }

    @Nested
    @DisplayName("Expired: estado terminal, no admite ninguna operación")
    class DesdeExpired {

        private Ticket ticketExpirado() {
            Ticket t = ticketEsperando();
            t.expirar();
            return t;
        }

        @Test
        void cancelar_lanza_TransicionInvalidaException() {
            Ticket t = ticketExpirado();
            assertThatThrownBy(t::cancelar)
                    .isInstanceOf(TransicionInvalidaException.class)
                    .hasMessageContaining("EXPIRED");
        }

        @Test
        void emparejar_lanza_TransicionInvalidaException() {
            Ticket t = ticketExpirado();
            assertThatThrownBy(() -> t.emparejar(PARTIDA))
                    .isInstanceOf(TransicionInvalidaException.class)
                    .hasMessageContaining("EXPIRED");
        }

        @Test
        void expirar_lanza_TransicionInvalidaException() {
            Ticket t = ticketExpirado();
            assertThatThrownBy(t::expirar)
                    .isInstanceOf(TransicionInvalidaException.class)
                    .hasMessageContaining("EXPIRED");
        }
    }

    @Nested
    @DisplayName("Nombre del estado y reconstrucción (fromName)")
    class NombreYReconstruccion {

        @Test
        void cada_estado_expone_su_nombre_en_mayusculas() {
            assertThat(new Waiting().name()).isEqualTo("WAITING");
            assertThat(new Matched().name()).isEqualTo("MATCHED");
            assertThat(new Cancelled().name()).isEqualTo("CANCELLED");
            assertThat(new Expired().name()).isEqualTo("EXPIRED");
        }

        @Test
        void fromName_reconstruye_cada_estado_conocido() {
            assertThat(TicketState.fromName("WAITING")).isInstanceOf(Waiting.class);
            assertThat(TicketState.fromName("MATCHED")).isInstanceOf(Matched.class);
            assertThat(TicketState.fromName("CANCELLED")).isInstanceOf(Cancelled.class);
            assertThat(TicketState.fromName("EXPIRED")).isInstanceOf(Expired.class);
        }

        @Test
        void fromName_lanza_si_el_nombre_es_desconocido_o_nulo() {
            assertThatThrownBy(() -> TicketState.fromName("CONFIRMING"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CONFIRMING");
            assertThatThrownBy(() -> TicketState.fromName(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void fromName_y_name_roundtrip() {
            for (TicketState estado : List.of(new Waiting(), new Matched(), new Cancelled(), new Expired())) {
                assertThat(TicketState.fromName(estado.name())).isInstanceOf(estado.getClass());
            }
        }
    }

    @Test
    void un_ticket_nuevo_nace_en_Waiting() {
        Ticket t = ticketEsperando();
        assertThat(t.estado()).isInstanceOf(Waiting.class);
        assertThatCode(t::cancelar).doesNotThrowAnyException();
    }
}
