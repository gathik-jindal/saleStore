package com.salestop.edge;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.WebExchangeBindException;

@RestControllerAdvice
class EdgeExceptionHandler {
  @ExceptionHandler(JwtVerifier.Unauthorized.class)
  ResponseEntity<?> unauthorized(Exception e) {
    return error(HttpStatus.UNAUTHORIZED, e.getMessage());
  }

  @ExceptionHandler(CheckoutController.BadRequest.class)
  ResponseEntity<?> bad(Exception e) {
    return error(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(CheckoutController.Conflict.class)
  ResponseEntity<?> conflict(Exception e) {
    return error(HttpStatus.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(CheckoutController.Unavailable.class)
  ResponseEntity<?> unavailable(Exception e) {
    return error(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
  }

  @ExceptionHandler(CheckoutController.TooManyRequests.class)
  ResponseEntity<?> limited() {
    return error(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
  }

  @ExceptionHandler(WebExchangeBindException.class)
  ResponseEntity<?> validation(WebExchangeBindException e) {
    return error(HttpStatus.BAD_REQUEST, "Request validation failed");
  }

  private ResponseEntity<?> error(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .body(
            Map.of(
                "timestamp",
                Instant.now(),
                "status",
                status.value(),
                "error",
                status.getReasonPhrase(),
                "message",
                message == null ? status.getReasonPhrase() : message));
  }
}
