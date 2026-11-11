package com.killerprotocol.matchmaking.adapter.out.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.killerprotocol.matchmaking.application.port.in.LoginUseCase.AuthToken;
import com.killerprotocol.matchmaking.application.port.out.TokenProvider;
import com.killerprotocol.matchmaking.domain.model.PlayerId;
import com.killerprotocol.matchmaking.domain.model.Role;
import com.killerprotocol.matchmaking.domain.model.UserAccount;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProvider {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final byte[] secretKey;
    private final long expirationSec;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    @Autowired
    public JwtTokenProvider(
            @Value("${security.jwt.secret:killer-protocol-default-jwt-secret-key-2026-min-32-bytes}") String secret,
            @Value("${security.jwt.expiration-sec:900}") long expirationSec) {
        this(secret, expirationSec, Clock.systemUTC());
    }

    public JwtTokenProvider(String secret, long expirationSec, Clock clock) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("El secreto JWT debe tener al menos 32 bytes.");
        }
        if (expirationSec <= 0) {
            throw new IllegalArgumentException("El tiempo de expiración del JWT debe ser positivo.");
        }
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSec = expirationSec;
        this.clock = clock;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public AuthToken generateToken(UserAccount account) {
        try {
            long issuedAt = clock.instant().getEpochSecond();
            long expiresAt = issuedAt + expirationSec;

            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", account.playerId().value());
            payload.put("email", account.email());
            payload.put("role", account.role().name());
            payload.put("iat", issuedAt);
            payload.put("exp", expiresAt);

            String encodedHeader = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(header));
            String encodedPayload = URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
            String signingInput = encodedHeader + "." + encodedPayload;
            String signature = URL_ENCODER.encodeToString(sign(signingInput));

            return new AuthToken(signingInput + "." + signature, "Bearer", expirationSec);
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible generar el token JWT.", e);
        }
    }

    @Override
    public Optional<TokenClaims> validateAndExtract(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return Optional.empty();
        }
        try {
            String signingInput = parts[0] + "." + parts[1];
            byte[] expectedSignature = sign(signingInput);
            byte[] providedSignature = URL_DECODER.decode(parts[2]);
            if (!MessageDigest.isEqual(expectedSignature, providedSignature)) {
                return Optional.empty();
            }

            byte[] payloadBytes = URL_DECODER.decode(parts[1]);
            Map<String, Object> claims = objectMapper.readValue(payloadBytes, new TypeReference<>() { });

            Number exp = (Number) claims.get("exp");
            if (exp == null || clock.instant().getEpochSecond() >= exp.longValue()) {
                return Optional.empty();
            }

            String sub = (String) claims.get("sub");
            String email = (String) claims.get("email");
            String roleName = (String) claims.get("role");
            if (sub == null || email == null || roleName == null) {
                return Optional.empty();
            }

            return Optional.of(new TokenClaims(new PlayerId(sub), email, Role.valueOf(roleName)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private byte[] sign(String input) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGO);
        mac.init(new SecretKeySpec(secretKey, HMAC_ALGO));
        return mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
    }
}
