package com.salestop.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.salestop.common.events.PaymentStatusEvent;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class NotificationHubTest {
  @Test
  void storesAndStreamsPaymentUpdates() {
    NotificationHub hub = new NotificationHub();
    UUID user = UUID.randomUUID();
    var stream = hub.stream(user);
    StepVerifier.create(stream.take(1))
        .then(
            () ->
                hub.payment(
                    new PaymentStatusEvent(
                        UUID.randomUUID(), user, "SUCCEEDED", "demo", Instant.now())))
        .assertNext(n -> assertThat(n.type()).isEqualTo("PAYMENT_SUCCEEDED"))
        .verifyComplete();
    assertThat(hub.recent(user)).hasSize(1);
  }
}
