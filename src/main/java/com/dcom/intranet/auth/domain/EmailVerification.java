package com.dcom.intranet.auth.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", length = 50)
    private String loginId;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "verification_code", nullable = false, length = 6)
    private String code;

    @Column(name = "email_change_token", unique = true, length = 100)
    private String emailChangeToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean verified;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    /**
     * Auth 회원가입 이메일 인증용
     */
    public EmailVerification(String email, String code, int expirationMinutes) {
        this.email = email;
        this.code = code;
        this.expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        this.verified = false;
        this.used = false;
    }

    /**
     * MyPage 이메일 변경 인증용
     */
    public static EmailVerification create(String loginId, String email,
                                           String verificationCode, LocalDateTime expiresAt) {
        EmailVerification ev = new EmailVerification();
        ev.loginId = loginId;
        ev.email = email;
        ev.code = verificationCode;
        ev.expiresAt = expiresAt;
        ev.verified = false;
        ev.used = false;
        return ev;
    }

    // ===== Auth용 메서드 =====

    public void verify() {
        this.verified = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    // ===== MyPage용 메서드 =====

    public boolean isActive(LocalDateTime now) {
        return !used && expiresAt.isAfter(now);
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean matchesCode(String verificationCode) {
        return this.code.equals(verificationCode);
    }

    public void verify(String emailChangeToken) {
        this.emailChangeToken = emailChangeToken;
        this.verified = true;
    }

    public boolean belongsTo(String loginId) {
        return this.loginId.equals(loginId);
    }

    public boolean canChangeEmail() {
        return verified && !used;
    }

    public void markUsed() {
        this.used = true;
    }

    // ===== Getter (Lombok @Getter로 대부분 커버, 추가 필요한 것) =====

    public String getVerificationCode() {
        return this.code;
    }
}