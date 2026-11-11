package com.killerprotocol.matchmaking.adapter.in.web;

import com.killerprotocol.matchmaking.application.port.in.LoginUseCase;
import com.killerprotocol.matchmaking.application.port.in.LoginUseCase.AuthToken;
import com.killerprotocol.matchmaking.application.port.in.RegisterUserUseCase;
import com.killerprotocol.matchmaking.domain.model.PlayerId;
import com.killerprotocol.matchmaking.domain.model.Role;
import com.killerprotocol.matchmaking.domain.model.UserAccount;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUser;
    private final LoginUseCase loginUser;

    public AuthController(RegisterUserUseCase registerUser, LoginUseCase loginUser) {
        this.registerUser = registerUser;
        this.loginUser = loginUser;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserAccountView register(@Valid @RequestBody RegisterRequest request) {
        UserAccount account = registerUser.register(
                new PlayerId(request.playerId()),
                request.email(),
                request.password(),
                request.role()
        );
        return UserAccountView.from(account);
    }

    @PostMapping("/login")
    public AuthToken login(@Valid @RequestBody LoginRequest request) {
        return loginUser.login(request.email(), request.password());
    }

    public record RegisterRequest(
            @NotBlank String playerId,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 128) String password,
            @NotNull Role role
    ) { }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) { }

    public record UserAccountView(String playerId, String email, Role role, Instant createdAt) {
        static UserAccountView from(UserAccount account) {
            return new UserAccountView(
                    account.playerId().value(),
                    account.email(),
                    account.role(),
                    account.createdAt()
            );
        }
    }
}
