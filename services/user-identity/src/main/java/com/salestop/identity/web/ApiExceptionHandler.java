package com.salestop.identity.web;

import com.salestop.identity.security.InvalidTokenException;
import com.salestop.identity.service.ConflictException;
import com.salestop.identity.service.UnauthorizedException;
import com.salestop.identity.web.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ConflictException.class)
  ResponseEntity<ErrorResponse> conflict(ConflictException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of("conflict", ex.getMessage()));
  }

  @ExceptionHandler({UnauthorizedException.class, InvalidTokenException.class})
  ResponseEntity<ErrorResponse> unauthorized(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponse.of("unauthorized", ex.getMessage()));
  }

  @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
  ResponseEntity<ErrorResponse> badRequest(Exception ex) {
    return ResponseEntity.badRequest().body(ErrorResponse.of("bad_request", "Request is invalid"));
  }
}
