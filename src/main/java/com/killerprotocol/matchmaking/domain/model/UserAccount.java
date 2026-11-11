package com.killerprotocol.matchmaking.domain.model;

import java.time.Instant;

public record UserAccount(PlayerId playerId, String email, String passwordHash, Role role, Instant createdAt) {

    public UserAccount {
        if (playerId == null) {
            throw new IllegalArgumentException("El identificador del jugador es obligatorio.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
        if (passwordHash == null || passwordHash.isBlank() || !passwordHash.startsWith("$argon2")) {
            throw new IllegalArgumentException("La contraseña debe almacenarse como un hash Argon2 válido.");
        }
        if (role == null) {
            throw new IllegalArgumentException("El rol del usuario es obligatorio.");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("La fecha de creación es obligatoria.");
        }
        email = email.trim().toLowerCase();
    }
}
