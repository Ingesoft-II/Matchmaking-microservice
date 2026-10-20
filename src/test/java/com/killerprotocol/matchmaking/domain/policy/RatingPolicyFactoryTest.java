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

class RatingPolicyFactoryTest {

    private static final Instant CREACION = Instant.parse("2026-10-16T22:10:00Z");

    private final PolicySettings settings = new PolicySettings(
            new PolicySettings.RankedSettings(100, 2.0),
            new PolicySettings.ArcadeSettings(120));

    private Ticket ticket(String id, int rating, int latencyMs) {
        Ticket t = new Ticket(new TicketId(id),
                new Party(new PartyId("pa-" + id), List.of(new PlayerId("p-" + id))),
                GameMode.RANKED, "sa-east", latencyMs, CREACION);
        t.asignarRating(rating);
        return t;
    }

    @Test
    @DisplayName("RANKED produce una RankedPolicy")
    void rankedProduceRankedPolicy() {
        RatingPolicy policy = RatingPolicyFactory.create(GameMode.RANKED, settings);
        assertThat(policy).isInstanceOf(RankedPolicy.class);
    }

    @Test
    @DisplayName("ARCADE produce una ArcadePolicy")
    void arcadeProduceArcadePolicy() {
        RatingPolicy policy = RatingPolicyFactory.create(GameMode.ARCADE, settings);
        assertThat(policy).isInstanceOf(ArcadePolicy.class);
    }

    @Test
    @DisplayName("La política creada usa exactamente los parámetros dados, no valores fijos")
    void laPoliticaCreadaUsaLosParametrosDados() {
        PolicySettings estrictos = new PolicySettings(
                new PolicySettings.RankedSettings(10, 0.0),
                new PolicySettings.ArcadeSettings(50));

        Ticket a = ticket("t-1", 1500, 38);
        RatingPolicy ranked = RatingPolicyFactory.create(GameMode.RANKED, estrictos);
        assertThat(ranked.canMatch(a, ticket("t-2", 1510, 38), 0)).isTrue();  // Δ = 10 (frontera)
        assertThat(ranked.canMatch(a, ticket("t-2", 1511, 38), 0)).isFalse(); // Δ = 11 > 10
        assertThat(ranked.canMatch(a, ticket("t-2", 1511, 38), 100)).isFalse(); // crecimiento 0

        RatingPolicy arcade = RatingPolicyFactory.create(GameMode.ARCADE, estrictos);
        assertThat(arcade.canMatch(a, ticket("t-3", 1500, 50), 0)).isTrue();   // latencia 50 (frontera)
        assertThat(arcade.canMatch(a, ticket("t-3", 1500, 51), 0)).isFalse();  // latencia 51 > 50
    }

    @Test
    @DisplayName("create valida sus argumentos")
    void createValidaSusArgumentos() {
        assertThatThrownBy(() -> RatingPolicyFactory.create(null, settings))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RatingPolicyFactory.create(GameMode.RANKED, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("PolicySettings valida la presencia y el rango de los parámetros por modo")
    void policySettingsValidaLosParametrosPorModo() {
        assertThatThrownBy(() -> new PolicySettings(null, new PolicySettings.ArcadeSettings(120)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PolicySettings(new PolicySettings.RankedSettings(100, 2.0), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PolicySettings.RankedSettings(-1, 2.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PolicySettings.RankedSettings(100, -2.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PolicySettings.ArcadeSettings(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
