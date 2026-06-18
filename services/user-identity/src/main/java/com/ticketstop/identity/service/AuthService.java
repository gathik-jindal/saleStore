package com.ticketstop.identity.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketstop.identity.domain.RefreshSession;
import com.ticketstop.identity.domain.UserAccount;
import com.ticketstop.identity.repository.RefreshSessionRepository;
import com.ticketstop.identity.repository.UserAccountRepository;
import com.ticketstop.identity.security.JwtPrincipal;
import com.ticketstop.identity.security.JwtService;
import com.ticketstop.identity.web.dto.AuthResponse;
import com.ticketstop.identity.web.dto.UserResponse;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final RefreshSessionRepository sessions;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
        UserAccountRepository users,
        RefreshSessionRepository sessions,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.users = users;
        this.sessions = sessions;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(String email, String password, String fullName) {
        String normalizedEmail = normalize(email);
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email is already registered");
        }
        UserAccount user = users.save(new UserAccount(
            UUID.randomUUID(),
            normalizedEmail,
            passwordEncoder.encode(password),
            fullName.trim()
        ));
        return tokensFor(user);
    }

    @Transactional
    public AuthResponse login(String email, String password) {
        UserAccount user = users.findByEmailIgnoreCase(normalize(email))
            .filter(UserAccount::isEnabled)
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return tokensFor(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        String hash = hash(refreshToken);
        RefreshSession session = sessions.findByTokenHash(hash)
            .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid"));
        if (!session.isUsable(OffsetDateTime.now())) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }
        session.revoke();
        return tokensFor(session.getUser());
    }

    @Transactional
    public void logout(String refreshToken) {
        sessions.findByTokenHash(hash(refreshToken)).ifPresent(RefreshSession::revoke);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String accessToken) {
        JwtPrincipal principal = jwtService.validate(accessToken);
        UserAccount user = users.findById(principal.userId())
            .filter(UserAccount::isEnabled)
            .orElseThrow(() -> new UnauthorizedException("User no longer exists"));
        return UserResponse.from(user);
    }

    private AuthResponse tokensFor(UserAccount user) {
        String refreshToken = newRefreshToken();
        sessions.save(new RefreshSession(
            UUID.randomUUID(),
            user,
            hash(refreshToken),
            jwtService.refreshTokenExpiry()
        ));
        return new AuthResponse(jwtService.issueAccessToken(user), refreshToken, "Bearer", UserResponse.from(user));
    }

    private String newRefreshToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash refresh token", ex);
        }
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
