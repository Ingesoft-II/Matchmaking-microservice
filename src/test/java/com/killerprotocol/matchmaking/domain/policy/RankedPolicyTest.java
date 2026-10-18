package com.killerprotocol.matchmaking.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.killerprotocol.matchmaking.domain.model.GameMode;
import com.killerprotocol.matchmaking.domain.model.Party;
import com.killerprotocol.matchmaking.domain.model.PartyId;
import com.killerprotocol.matchmaking.domain.model.PlayerId;
import com.killerprotocol.matchmaking.domain.model.Ticket;
import com.killerprotocol.matchmaking.domain.model.TicketId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RankedPolicyTest {

    private static final Instant CREACION = Instant.parse("2026-10-16T22:10:00Z");
    private final RankedPolicy policy = new RankedPolicy(100, 2.0);

    private Ticket ticket(String id, int rating) {
        Ticket t = new Ticket(new TicketId(id),
                new Party(new PartyId("pa-" + id), List.of(new PlayerId("p-" + id))),
                GameMode.RANKED, "sa-east", 38, CREACION);
        t.asignarRating(rating);
        return t;
    }

    @Test
    @DisplayName("Son compatibles si la diferencia no supera baseGap (espera 0)")
    void respetaElMargenBase() {
        Ticket a = ticket("t-1", 1500);
        assertThat(policy.canMatch(a, ticket("t-2", 1500), 0)).isTrue();  // Δ = 0
        assertThat(policy.canMatch(a, ticket("t-2", 1599), 0)).isTrue();  // Δ = 99
        assertThat(policy.canMatch(a, ticket("t-2", 1600), 0)).isTrue();  // Δ = 100
        assertThat(policy.canMatch(a, ticket("t-2", 1601), 0)).isFalse(); // Δ = 101
    }

    @Test
    @DisplayName("El margen se amplía con la espera: |Δrating| <= baseGap + growthPerSec·waited")
    void elMargenSeAmpliaConLaEspera() {
        Ticket a = ticket("t-1", 1500);
        Ticket lejos = ticket("t-2", 1650); // Δ = 150 > 100
        assertThat(policy.canMatch(a, lejos, 0)).isFalse();   // 150 > 100 + 0
        assertThat(policy.canMatch(a, lejos, 24)).isFalse();  // 150 > 100 + 48
        assertThat(policy.canMatch(a, lejos, 25)).isTrue();   // 150 <= 100 + 50 
        assertThat(policy.canMatch(a, lejos, 60)).isTrue();   // 150 <= 100 + 120
    }

    @Test
    @DisplayName("La comparación es simétrica: canMatch(a,b) == canMatch(b,a)")
    void esSimetrica() {
        Ticket a = ticket("t-1", 1480);
        Ticket b = ticket("t-2", 1555);
        for (long espera : new long[] {0, 10, 37}) {
            assertThat(policy.canMatch(a, b, espera)).isEqualTo(policy.canMatch(b, a, espera));
        }
    }

    @Test
    @DisplayName("Una espera negativa se trata como 0 (nunca reduce el margen)")
    void unaEsperaNegativaNoReduceElMargen() {
        Ticket a = ticket("t-1", 1500);
        assertThat(policy.canMatch(a, ticket("t-2", 1550), -50)).isTrue();  // Δ = 50 <= 100
        assertThat(policy.canMatch(a, ticket("t-2", 1601), -50)).isFalse(); // Δ = 101 > 100
    }

    @Test
    @DisplayName("Liskov: canMatch nunca lanza; argumentos o rating nulos → false")
    void nuncaLanzaExcepciones() {
        Ticket a = ticket("t-1", 1500);
        assertThat(policy.canMatch(null, a, 0)).isFalse();
        assertThat(policy.canMatch(a, null, 0)).isFalse();
        Ticket sinRating = new Ticket(new TicketId("t-3"),
                new Party(new PartyId("pa-3"), List.of(new PlayerId("p-3"))),
                GameMode.RANKED, "sa-east", 38, CREACION);
        assertThat(policy.canMatch(a, sinRating, 0)).isFalse();
        assertThat(policy.canMatch(sinRating, sinRating, 100)).isFalse();
    }

    @Test
    @DisplayName("El constructor valida sus parámetros")
    void elConstructorValidaSusParametros() {
        assertThatThrownBy(() -> new RankedPolicy(-1, 2.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RankedPolicy(100, -0.5))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
