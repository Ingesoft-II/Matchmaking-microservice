package com.killerprotocol.matchmaking.application.port.in;

public interface LoginUseCase {

    record AuthToken(String accessToken, String tokenType, long expiresIn) { }

    AuthToken login(String email, String rawPassword);
}
