package com.dcom.intranet.mypage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "verification_code", nullable = false, length = 6)
    private String verificationCode;

    @Column(name = "email_change_token", unique = true, length = 100)
    private String emailChangeToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected EmailVerification() {
    }

    private EmailVerification(String loginId, String email, String verificationCode, LocalDateTime expiresAt) {
        this.loginId = loginId;
        this.email = email;
        this.verificationCode = verificationCode;
        this.expiresAt = expiresAt;
        this.verified = false;
        this.used = false;
    }

    public static EmailVerification create(
            String loginId,
            String email,
            String verificationCode,
            LocalDateTime expiresAt
    ) {
        return new EmailVerification(loginId, email, verificationCode, expiresAt);
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isActive(LocalDateTime now) {
        return !used && expiresAt.isAfter(now);
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean matchesCode(String verificationCode) {
        return this.verificationCode.equals(verificationCode);
    }

    public void verify(String emailChangeToken) {
        this.emailChangeToken = emailChangeToken;
        this.verified = true;
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getEmail() {
        return email;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public String getEmailChangeToken() {
        return emailChangeToken;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean isUsed() {
        return used;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
