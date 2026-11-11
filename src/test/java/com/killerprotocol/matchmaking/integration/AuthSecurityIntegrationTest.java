package com.killerprotocol.matchmaking.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.killerprotocol.matchmaking.domain.model.Role;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void endpointsExigenAutenticacionYResponden401SinToken() throws Exception {
        String queuePayload = """
                {
                  "playerIds": ["player-unauth"],
                  "region": "sa-east",
                  "latencyMs": 30
                }
                """;

        mockMvc.perform(post("/api/matchmaking/tickets/arcade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queuePayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(post("/api/matchmaking/tickets/ranked")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queuePayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(post("/api/matchmaking/cycle"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void cadaRolSoloPuedeUsarSuEndpointExclusivoYRecibe403EnLosDemas() throws Exception {
        String casualToken = registerAndLogin(Role.CASUAL);
        String competitivoToken = registerAndLogin(Role.COMPETITIVO);
        String moderadorToken = registerAndLogin(Role.MODERADOR);

        String queuePayload = """
                {
                  "playerIds": ["player-role-1"],
                  "region": "sa-east",
                  "latencyMs": 30
                }
                """;

        mockMvc.perform(post("/api/matchmaking/tickets/arcade")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + casualToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queuePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"));

        mockMvc.perform(post("/api/matchmaking/tickets/arcade")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + competitivoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queuePayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(post("/api/matchmaking/tickets/arcade")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + moderadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queuePayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(post("/api/matchmaking/tickets/ranked")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + competitivoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queuePayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.rating").value(1000));

        mockMvc.perform(post("/api/matchmaking/tickets/ranked")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + casualToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queuePayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(post("/api/matchmaking/tickets/ranked")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + moderadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(queuePayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(post("/api/matchmaking/cycle")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + moderadorToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/matchmaking/cycle")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + casualToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(post("/api/matchmaking/cycle")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + competitivoToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void jugadorCasualRecibe403AlIntentarModoRankedEnEndpointGeneral() throws Exception {
        String casualToken = registerAndLogin(Role.CASUAL);
        String competitivoToken = registerAndLogin(Role.COMPETITIVO);

        String rankedPayload = """
                {
                  "playerIds": ["player-ranked-test"],
                  "mode": "RANKED",
                  "region": "sa-east",
                  "latencyMs": 35
                }
                """;

        mockMvc.perform(post("/api/matchmaking/tickets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + casualToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rankedPayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(post("/api/matchmaking/tickets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + competitivoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rankedPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    private String registerAndLogin(Role role) throws Exception {
        String suffix = UUID.randomUUID().toString();
        String playerId = "player-" + suffix;
        String email = "user-" + suffix + "@killerprotocol.com";
        String password = "PasswordSegura123!";

        String registerPayload = """
                {
                  "playerId": "%s",
                  "email": "%s",
                  "password": "%s",
                  "role": "%s"
                }
                """.formatted(playerId, email, password, role.name());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated());

        String loginPayload = """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }
}
