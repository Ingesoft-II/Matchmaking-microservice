package com.killerprotocol.matchmaking.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    void matchmakingExigeTokenJwtYPermiteAccesoTrasRegistroYLogin() throws Exception {
        String ticketPayload = """
                {
                  "playerIds": ["player-auth-1"],
                  "mode": "ARCADE",
                  "region": "sa-east",
                  "latencyMs": 30
                }
                """;

        mockMvc.perform(post("/api/matchmaking/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketPayload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());

        String registerPayload = """
                {
                  "playerId": "player-auth-1",
                  "email": "auth1@killerprotocol.com",
                  "password": "PasswordSegura123!",
                  "role": "COMPETITIVO"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerId").value("player-auth-1"))
                .andExpect(jsonPath("$.email").value("auth1@killerprotocol.com"))
                .andExpect(jsonPath("$.role").value("COMPETITIVO"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        String loginPayload = """
                {
                  "email": "auth1@killerprotocol.com",
                  "password": "PasswordSegura123!"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = body.get("accessToken").asText();

        mockMvc.perform(post("/api/matchmaking/tickets")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ticketPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"));
    }
}
