package com.ticketstop.identity.grpc;

import org.springframework.stereotype.Component;

import com.ticketstop.identity.security.InvalidTokenException;
import com.ticketstop.identity.security.JwtPrincipal;
import com.ticketstop.identity.security.JwtService;

import io.grpc.stub.StreamObserver;

@Component
public class IdentityGrpcService extends IdentityKeyServiceGrpc.IdentityKeyServiceImplBase {
    private final JwtService jwtService;

    public IdentityGrpcService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void getPublicKey(PublicKeyRequest request, StreamObserver<PublicKeyResponse> responseObserver) {
        PublicKeyResponse response = PublicKeyResponse.newBuilder()
            .setKid(jwtService.keyId())
            .setPem(jwtService.publicKeyPem())
            .setJwksJson(jwtService.jwksJson())
            .setActive(true)
            .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        try {
            JwtPrincipal principal = jwtService.validate(request.getAccessToken());
            responseObserver.onNext(ValidateTokenResponse.newBuilder()
                .setValid(true)
                .setSubject(principal.userId().toString())
                .setEmail(principal.email())
                .addAllRoles(principal.roles())
                .build());
        } catch (InvalidTokenException ex) {
            responseObserver.onNext(ValidateTokenResponse.newBuilder()
                .setValid(false)
                .setReason(ex.getMessage())
                .build());
        }
        responseObserver.onCompleted();
    }
}
