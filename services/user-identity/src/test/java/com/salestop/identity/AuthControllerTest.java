package com.salestop.identity;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
@TestPropertySource(properties = "identity.grpc.port=0")
class AuthControllerTest {
  @Autowired private WebTestClient webTestClient;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void registerLoginMeAndRefreshWork() throws Exception {
    String registerJson =
        """
            {
              "email": "ada@example.com",
              "password": "correct-horse-battery-staple",
              "fullName": "Ada Lovelace"
            }
            """;

    String registerResponse =
        webTestClient
            .post()
            .uri("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(registerJson)
            .exchange()
            .expectStatus()
            .isCreated()
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();

    JsonNode registered = objectMapper.readTree(registerResponse);
    assertThat(registered.get("accessToken").asText()).isNotBlank();
    assertThat(registered.get("refreshToken").asText()).isNotBlank();

    webTestClient
        .get()
        .uri("/api/v1/auth/me")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + registered.get("accessToken").asText())
        .exchange()
        .expectStatus()
        .isOk();

    String loginJson =
        """
            {
              "email": "ada@example.com",
              "password": "correct-horse-battery-staple"
            }
            """;
    webTestClient
        .post()
        .uri("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(loginJson)
        .exchange()
        .expectStatus()
        .isOk();

    String refreshJson =
        """
            { "refreshToken": "%s" }
            """
            .formatted(registered.get("refreshToken").asText());
    webTestClient
        .post()
        .uri("/api/v1/auth/refresh")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(refreshJson)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  void duplicateRegistrationIsRejected() throws Exception {
    String json =
        """
            {
              "email": "grace@example.com",
              "password": "correct-horse-battery-staple",
              "fullName": "Grace Hopper"
            }
            """;

    webTestClient
        .post()
        .uri("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(json)
        .exchange()
        .expectStatus()
        .isCreated();
    webTestClient
        .post()
        .uri("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(json)
        .exchange()
        .expectStatus()
        .isEqualTo(409);
  }
}
