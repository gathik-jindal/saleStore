package com.salestop.identity.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "refresh_sessions")
public class RefreshSession {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected RefreshSession() {
    }

    public RefreshSession(UUID id, UserAccount user, String tokenHash, OffsetDateTime expiresAt) {
        this.id = id;
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }

    public void revoke() {
        revokedAt = OffsetDateTime.now();
    }

    public boolean isUsable(OffsetDateTime now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }

    public UUID getId() { return id; }
    public UserAccount getUser() { return user; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
}
