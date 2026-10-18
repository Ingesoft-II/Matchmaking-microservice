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

class ArcadePolicyTest {

    private static final Instant CREACION = Instant.parse("2026-10-16T22:10:00Z");

    private final ArcadePolicy policy = new ArcadePolicy(120);

    private Ticket ticket(String id, int latencyMs, Integer rating) {
        Ticket t = new Ticket(new TicketId(id),
                new Party(new PartyId("pa-" + id), List.of(new PlayerId("p-" + id))),
                GameMode.ARCADE, "sa-east", latencyMs, CREACION);
        if (rating != null) {
            t.asignarRating(rating);
        }
        return t;
    }

    @Test
    @DisplayName("Son compatibles si la latencia de ambos no supera el umbral")
    void respetaElUmbralDeLatencia() {
        Ticket a = ticket("t-1", 38, null);
        assertThat(policy.canMatch(a, ticket("t-2", 119, null), 0)).isTrue();
        assertThat(policy.canMatch(a, ticket("t-2", 120, null), 0)).isTrue();  // frontera
        assertThat(policy.canMatch(a, ticket("t-2", 121, null), 0)).isFalse();
        assertThat(policy.canMatch(ticket("t-3", 150, null), ticket("t-4", 10, null), 0)).isFalse();
    }

    @Test
    @DisplayName("El rating es irrelevante: tickets con rating muy distinto también son compatibles (RF_04)")
    void elRatingEsIrrelevante() {
        Ticket bajo = ticket("t-1", 40, 700);
        Ticket alto = ticket("t-2", 50, 2300);
        assertThat(policy.canMatch(bajo, alto, 0)).isTrue();
        assertThat(policy.canMatch(bajo, ticket("t-3", 60, null), 999)).isTrue();
    }

    @Test
    @DisplayName("La espera no afecta a la compatibilidad arcade")
    void laEsperaNoAfecta() {
        Ticket a = ticket("t-1", 38, null);
        assertThat(policy.canMatch(a, ticket("t-2", 44, null), 0)).isTrue();
        assertThat(policy.canMatch(a, ticket("t-2", 44, null), 3600)).isTrue();
        assertThat(policy.canMatch(a, ticket("t-2", 130, null), 3600)).isFalse();
    }

    @Test
    @DisplayName("Liskov: canMatch nunca lanza; argumentos nulos → false")
    void nuncaLanzaExcepciones() {
        Ticket a = ticket("t-1", 38, null);
        assertThat(policy.canMatch(null, a, 0)).isFalse();
        assertThat(policy.canMatch(a, null, 0)).isFalse();
    }

    @Test
    @DisplayName("El constructor valida su umbral")
    void elConstructorValidaSuUmbral() {
        assertThatThrownBy(() -> new ArcadePolicy(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
