package com.salestop.identity.web;

import com.salestop.identity.service.AuthService;
import com.salestop.identity.web.dto.AuthResponse;
import com.salestop.identity.web.dto.LoginRequest;
import com.salestop.identity.web.dto.RefreshRequest;
import com.salestop.identity.web.dto.RegisterRequest;
import com.salestop.identity.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public Mono<ResponseEntity<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
    return blocking(
            () -> authService.register(request.email(), request.password(), request.fullName()))
        .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
  }

  @PostMapping("/login")
  public Mono<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return blocking(() -> authService.login(request.email(), request.password()));
  }

  @PostMapping("/refresh")
  public Mono<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
    return blocking(() -> authService.refresh(request.refreshToken()));
  }

  @PostMapping("/logout")
  public Mono<ResponseEntity<Void>> logout(@Valid @RequestBody RefreshRequest request) {
    return blocking(
        () -> {
          authService.logout(request.refreshToken());
          return ResponseEntity.noContent().<Void>build();
        });
  }

  @GetMapping("/me")
  public Mono<UserResponse> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
    return blocking(() -> authService.currentUser(bearerToken(authorization)));
  }

  private String bearerToken(String authorization) {
    if (authorization == null || !authorization.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Authorization header must contain a Bearer token");
    }
    return authorization.substring("Bearer ".length());
  }

  private <T> Mono<T> blocking(java.util.concurrent.Callable<T> callable) {
    return Mono.fromCallable(callable).subscribeOn(Schedulers.boundedElastic());
  }
}
