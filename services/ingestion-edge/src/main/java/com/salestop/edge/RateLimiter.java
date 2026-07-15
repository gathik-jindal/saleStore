package com.salestop.edge;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
class RateLimiter {
  private static final DefaultRedisScript<Long> SCRIPT =
      new DefaultRedisScript<>(
          """
  local capacity=tonumber(ARGV[1]); local rate=tonumber(ARGV[2]); local now=tonumber(ARGV[3])
  local values=redis.call('HMGET',KEYS[1],'tokens','last')
  local tokens=tonumber(values[1]) or capacity; local last=tonumber(values[2]) or now
  tokens=math.min(capacity,tokens+((now-last)/1000.0)*rate)
  local allowed=0
  if tokens>=1 then tokens=tokens-1;allowed=1 end
  redis.call('HSET',KEYS[1],'tokens',tokens,'last',now);redis.call('PEXPIRE',KEYS[1],60000)
  if allowed==1 then return math.floor(tokens) else return -1 end
  """,
          Long.class);
  private final ReactiveStringRedisTemplate redis;
  private final int capacity;
  private final int rate;

  RateLimiter(
      ReactiveStringRedisTemplate redis,
      @Value("${edge.rate-limit.capacity:10}") int capacity,
      @Value("${edge.rate-limit.refill-per-second:10}") int rate) {
    this.redis = redis;
    this.capacity = capacity;
    this.rate = rate;
  }

  Mono<Decision> allow(String client) {
    return redis
        .execute(
            SCRIPT,
            List.of("rate-limit:" + client),
            List.of(
                Integer.toString(capacity),
                Integer.toString(rate),
                Long.toString(System.currentTimeMillis())))
        .next()
        .map(v -> new Decision(v >= 0, Math.max(v, 0)))
        .timeout(Duration.ofMillis(250));
  }

  record Decision(boolean allowed, long remaining) {}
}
