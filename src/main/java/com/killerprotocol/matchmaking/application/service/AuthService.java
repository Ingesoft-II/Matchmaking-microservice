package com.killerprotocol.matchmaking.application.service;

import com.killerprotocol.matchmaking.application.port.in.LoginUseCase;
import com.killerprotocol.matchmaking.application.port.in.RegisterUserUseCase;
import com.killerprotocol.matchmaking.application.port.out.PasswordHasher;
import com.killerprotocol.matchmaking.application.port.out.TokenProvider;
import com.killerprotocol.matchmaking.application.port.out.UserAccountStore;
import com.killerprotocol.matchmaking.domain.exception.CredencialesInvalidasException;
import com.killerprotocol.matchmaking.domain.exception.UsuarioYaExisteException;
import com.killerprotocol.matchmaking.domain.model.PlayerId;
import com.killerprotocol.matchmaking.domain.model.Role;
import com.killerprotocol.matchmaking.domain.model.UserAccount;
import java.time.Instant;

public class AuthService implements RegisterUserUseCase, LoginUseCase {

    private final UserAccountStore accounts;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public AuthService(UserAccountStore accounts, PasswordHasher passwordHasher, TokenProvider tokenProvider) {
        this.accounts = accounts;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public UserAccount register(PlayerId playerId, String email, String rawPassword, Role role) {
        if (rawPassword == null || rawPassword.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (accounts.findByPlayerId(playerId).isPresent()) {
            throw new UsuarioYaExisteException("El jugador ya tiene una cuenta registrada: " + playerId.value());
        }
        if (accounts.findByEmail(normalizedEmail).isPresent()) {
            throw new UsuarioYaExisteException("El correo electrónico ya está registrado: " + normalizedEmail);
        }
        String passwordHash = passwordHasher.hash(rawPassword);
        UserAccount account = new UserAccount(playerId, normalizedEmail, passwordHash, role, Instant.now());
        accounts.save(account);
        return account;
    }

    @Override
    public AuthToken login(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            throw new CredencialesInvalidasException("Credenciales inválidas.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        UserAccount account = accounts.findByEmail(normalizedEmail)
                .orElseThrow(() -> new CredencialesInvalidasException("Credenciales inválidas."));
        if (!passwordHasher.matches(rawPassword, account.passwordHash())) {
            throw new CredencialesInvalidasException("Credenciales inválidas.");
        }
        return tokenProvider.generateToken(account);
    }
}
