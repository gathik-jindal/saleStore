package com.salestop.inventory;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
class ReservationService {
  private static final DefaultRedisScript<String> RESERVE =
      new DefaultRedisScript<>(
          """
  local existing = redis.call('GET', KEYS[2])
  if existing then
    local current = redis.call('GET', KEYS[1]) or '-1'
    return 'DUPLICATE|' .. existing .. '|' .. current
  end
  local buyer = redis.call('GET', KEYS[3])
  if buyer then
    local current = redis.call('GET', KEYS[1]) or '-1'
    return 'ALREADY_RESERVED|' .. buyer .. '|' .. current
  end
  local stock = tonumber(redis.call('GET', KEYS[1]) or '-1')
  local quantity = tonumber(ARGV[1])
  if stock < quantity then return 'SOLD_OUT||' .. stock end
  local remaining = redis.call('DECRBY', KEYS[1], quantity)
  redis.call('SET', KEYS[2], ARGV[2], 'PX', ARGV[3])
  redis.call('SET', KEYS[3], ARGV[2], 'PX', ARGV[4])
  return 'RESERVED|' .. ARGV[2] .. '|' .. remaining
  """,
          String.class);
  private static final DefaultRedisScript<Long> RELEASE =
      new DefaultRedisScript<>(
          """
  if redis.call('GET', KEYS[2]) ~= ARGV[1] then return 0 end
  redis.call('INCRBY', KEYS[1], ARGV[2])
  redis.call('DEL', KEYS[2], KEYS[3])
  return 1
  """,
          Long.class);

  private final StringRedisTemplate redis;
  private final Duration idempotencyTtl;
  private final Duration buyerTtl;

  ReservationService(
      StringRedisTemplate redis,
      @Value("${inventory.idempotency-ttl:PT5M}") Duration idempotencyTtl,
      @Value("${inventory.buyer-ttl:PT24H}") Duration buyerTtl) {
    this.redis = redis;
    this.idempotencyTtl = idempotencyTtl;
    this.buyerTtl = buyerTtl;
  }

  ReservationResult reserve(
      long productId, UUID userId, int quantity, String idempotencyKey, UUID proposedOrderId) {
    List<String> keys =
        List.of(stockKey(productId), "idempotency:" + idempotencyKey, buyerKey(productId, userId));
    String raw =
        redis.execute(
            RESERVE,
            keys,
            Integer.toString(quantity),
            proposedOrderId.toString(),
            Long.toString(idempotencyTtl.toMillis()),
            Long.toString(buyerTtl.toMillis()));
    if (raw == null) throw new IllegalStateException("Redis reservation script returned no result");
    String[] parts = raw.split("\\|", -1);
    UUID orderId = parts[1].isBlank() ? null : UUID.fromString(parts[1]);
    return new ReservationResult(Status.valueOf(parts[0]), orderId, Integer.parseInt(parts[2]));
  }

  boolean release(long productId, UUID userId, int quantity, String idempotencyKey, UUID orderId) {
    Long released =
        redis.execute(
            RELEASE,
            List.of(
                stockKey(productId), "idempotency:" + idempotencyKey, buyerKey(productId, userId)),
            orderId.toString(),
            Integer.toString(quantity));
    return Long.valueOf(1).equals(released);
  }

  void seed(long productId, int stock) {
    redis.opsForValue().set(stockKey(productId), Integer.toString(stock));
  }

  int availability(long productId) {
    String value = redis.opsForValue().get(stockKey(productId));
    return value == null ? -1 : Integer.parseInt(value);
  }

  private String stockKey(long id) {
    return "flashsale:product:" + id + ":stock";
  }

  private String buyerKey(long id, UUID user) {
    return "flashsale:product:" + id + ":user:" + user;
  }

  enum Status {
    RESERVED,
    DUPLICATE,
    ALREADY_RESERVED,
    SOLD_OUT
  }

  record ReservationResult(Status status, UUID orderId, int remainingStock) {}
}
