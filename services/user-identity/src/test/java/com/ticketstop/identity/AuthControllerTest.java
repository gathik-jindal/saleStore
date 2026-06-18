package com.ticketstop.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "identity.grpc.port=0")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginMeAndRefreshWork() throws Exception {
        String registerJson = """
            {
              "email": "ada@example.com",
              "password": "correct-horse-battery-staple",
              "fullName": "Ada Lovelace"
            }
            """;

        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode registered = objectMapper.readTree(registerResponse);
        assertThat(registered.get("accessToken").asText()).isNotBlank();
        assertThat(registered.get("refreshToken").asText()).isNotBlank();

        mockMvc.perform(get("/api/v1/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + registered.get("accessToken").asText()))
            .andExpect(status().isOk());

        String loginJson = """
            {
              "email": "ada@example.com",
              "password": "correct-horse-battery-staple"
            }
            """;
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andExpect(status().isOk());

        String refreshJson = """
            { "refreshToken": "%s" }
            """.formatted(registered.get("refreshToken").asText());
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshJson))
            .andExpect(status().isOk());
    }

    @Test
    void duplicateRegistrationIsRejected() throws Exception {
        String json = """
            {
              "email": "grace@example.com",
              "password": "correct-horse-battery-staple",
              "fullName": "Grace Hopper"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isConflict());
    }
}
