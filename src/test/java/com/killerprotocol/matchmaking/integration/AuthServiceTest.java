package com.killerprotocol.matchmaking.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.killerprotocol.matchmaking.adapter.out.memory.InMemoryUserAccountStore;
import com.killerprotocol.matchmaking.adapter.out.security.Argon2PasswordHasher;
import com.killerprotocol.matchmaking.adapter.out.security.JwtTokenProvider;
import com.killerprotocol.matchmaking.application.port.in.LoginUseCase.AuthToken;
import com.killerprotocol.matchmaking.application.port.out.TokenProvider.TokenClaims;
import com.killerprotocol.matchmaking.application.service.AuthService;
import com.killerprotocol.matchmaking.domain.exception.CredencialesInvalidasException;
import com.killerprotocol.matchmaking.domain.exception.UsuarioYaExisteException;
import com.killerprotocol.matchmaking.domain.model.PlayerId;
import com.killerprotocol.matchmaking.domain.model.Role;
import com.killerprotocol.matchmaking.domain.model.UserAccount;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private static final String SECRET = "test-secret-key-for-killer-protocol-jwt-hs256-min-32-bytes";

    private InMemoryUserAccountStore store;
    private Argon2PasswordHasher hasher;
    private JwtTokenProvider tokenProvider;
    private AuthService authService;
    private Clock baseClock;

    @BeforeEach
    void setUp() {
        store = new InMemoryUserAccountStore();
        hasher = new Argon2PasswordHasher();
        baseClock = Clock.fixed(Instant.parse("2026-11-10T20:00:00Z"), ZoneOffset.UTC);
        tokenProvider = new JwtTokenProvider(SECRET, 900, baseClock);
        authService = new AuthService(store, hasher, tokenProvider);
    }

    @Test
    void registerGuardaHashArgon2YNuncaTextoPlano() {
        String rawPassword = "MiPasswordSegura123!";
        UserAccount account = authService.register(
                new PlayerId("player-1"),
                "player1@killerprotocol.com",
                rawPassword,
                Role.COMPETITIVO
        );

        assertThat(account.passwordHash()).isNotEqualTo(rawPassword);
        assertThat(account.passwordHash()).startsWith("$argon2id$");
        assertThat(hasher.matches(rawPassword, account.passwordHash())).isTrue();
    }

    @Test
    void registerRechazaCorreoOJugadorDuplicado() {
        authService.register(new PlayerId("player-1"), "player1@killerprotocol.com", "MiPasswordSegura123!", Role.CASUAL);

        assertThatThrownBy(() -> authService.register(
                new PlayerId("player-1"), "otro@killerprotocol.com", "MiPasswordSegura123!", Role.CASUAL))
                .isInstanceOf(UsuarioYaExisteException.class);

        assertThatThrownBy(() -> authService.register(
                new PlayerId("player-2"), "PLAYER1@killerprotocol.com", "MiPasswordSegura123!", Role.CASUAL))
                .isInstanceOf(UsuarioYaExisteException.class);
    }

    @Test
    void loginEmiteAccessTokenDeVidaCortaConClaimsValidos() {
        authService.register(new PlayerId("player-1"), "player1@killerprotocol.com", "MiPasswordSegura123!", Role.COMPETITIVO);

        AuthToken authToken = authService.login("player1@killerprotocol.com", "MiPasswordSegura123!");

        assertThat(authToken.tokenType()).isEqualTo("Bearer");
        assertThat(authToken.expiresIn()).isEqualTo(900);

        Optional<TokenClaims> claims = tokenProvider.validateAndExtract(authToken.accessToken());
        assertThat(claims).isPresent();
        assertThat(claims.get().playerId()).isEqualTo(new PlayerId("player-1"));
        assertThat(claims.get().email()).isEqualTo("player1@killerprotocol.com");
        assertThat(claims.get().role()).isEqualTo(Role.COMPETITIVO);
    }

    @Test
    void loginRechazaCredencialesIncorrectas() {
        authService.register(new PlayerId("player-1"), "player1@killerprotocol.com", "MiPasswordSegura123!", Role.CASUAL);

        assertThatThrownBy(() -> authService.login("player1@killerprotocol.com", "PasswordIncorrecta123!"))
                .isInstanceOf(CredencialesInvalidasException.class);

        assertThatThrownBy(() -> authService.login("inexistente@killerprotocol.com", "MiPasswordSegura123!"))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    void tokenExpiradoNoSuperaLaValidacion() {
        UserAccount account = authService.register(
                new PlayerId("player-1"), "player1@killerprotocol.com", "MiPasswordSegura123!", Role.CASUAL);
        AuthToken authToken = tokenProvider.generateToken(account);

        Clock expiredClock = Clock.offset(baseClock, Duration.ofSeconds(901));
        JwtTokenProvider futureValidator = new JwtTokenProvider(SECRET, 900, expiredClock);

        assertThat(futureValidator.validateAndExtract(authToken.accessToken())).isEmpty();
    }
}
