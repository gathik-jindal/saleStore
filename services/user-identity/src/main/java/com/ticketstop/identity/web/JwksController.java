package com.ticketstop.identity.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketstop.identity.security.JwtService;

import reactor.core.publisher.Mono;

@RestController
public class JwksController {
    private final JwtService jwtService;

    public JwksController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> jwks() {
        return Mono.just(jwtService.jwksJson());
    }
}
