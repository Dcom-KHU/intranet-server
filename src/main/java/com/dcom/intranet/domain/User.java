package com.dcom.intranet.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 빈 객체 만드는 거 제한
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String studentId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    @PrePersist
    /// JPA가 DB에 저장하기 직전에 호출하기때문에 저장지점의 정확한 시각을 보장함. (PrePersist)
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /// 회원가입용 생성자
    public User(String loginId, String password, String name,
                String studentId, String email, String phoneNumber) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.studentId = studentId;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = UserRole.USER;
        this.status = UserStatus.PENDING;
    }

    /// 최근 로그인 시각 갱신
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}