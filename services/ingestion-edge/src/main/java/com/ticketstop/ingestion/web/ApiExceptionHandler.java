package com.ticketstop.ingestion.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, Object> badRequest(Exception ex) {
        return Map.of("timestamp", Instant.now(), "error", ex instanceof MethodArgumentNotValidException ? "Request validation failed" : ex.getMessage());
    }

    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    Map<String, Object> downstream(RestClientException ex) {
        return Map.of("timestamp", Instant.now(), "error", "Downstream service call failed", "detail", ex.getMessage());
    }
}
