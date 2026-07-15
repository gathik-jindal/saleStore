package com.salestop.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {"spring.kafka.listener.auto-startup=false", "inventory.grpc-port=0"})
class ReservationConcurrencyTest {
  @Container
  static final GenericContainer<?> REDIS =
      new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine")).withExposedPorts(6379);

  @DynamicPropertySource
  static void redis(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
  }

  @Autowired ReservationService reservations;

  @Test
  void concurrentBuyersCannotOversellAndRetriesAreStable() throws Exception {
    long product = 991;
    reservations.seed(product, 1);
    UUID firstUser = UUID.randomUUID(), secondUser = UUID.randomUUID();
    UUID firstOrder = UUID.randomUUID(), secondOrder = UUID.randomUUID();
    try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
      var first =
          pool.submit(() -> reservations.reserve(product, firstUser, 1, "first", firstOrder));
      var second =
          pool.submit(() -> reservations.reserve(product, secondUser, 1, "second", secondOrder));
      var results = List.of(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS));
      assertThat(results)
          .extracting(ReservationService.ReservationResult::status)
          .containsExactlyInAnyOrder(
              ReservationService.Status.RESERVED, ReservationService.Status.SOLD_OUT);
      assertThat(reservations.availability(product)).isZero();
      var winner =
          results.stream()
              .filter(r -> r.status() == ReservationService.Status.RESERVED)
              .findFirst()
              .orElseThrow();
      String key = winner.orderId().equals(firstOrder) ? "first" : "second";
      UUID user = winner.orderId().equals(firstOrder) ? firstUser : secondUser;
      var retry = reservations.reserve(product, user, 1, key, UUID.randomUUID());
      assertThat(retry.status()).isEqualTo(ReservationService.Status.DUPLICATE);
      assertThat(retry.orderId()).isEqualTo(winner.orderId());
      assertThat(reservations.availability(product)).isZero();
    }
  }
}
