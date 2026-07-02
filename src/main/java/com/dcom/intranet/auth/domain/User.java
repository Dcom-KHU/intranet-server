package com.dcom.intranet.auth.domain;

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

    private LocalDateTime withdrawnAt;

    @Column
    private String tempPassword;

    @Column
    private LocalDateTime tempPasswordExpiresAt;

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

    /// 임시 비밀번호 설정
    public void setTempPassword(String encodedTempPassword, int expirationMinutes){
        this.tempPassword = encodedTempPassword;
        this.tempPasswordExpiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
    }

    /// 임시 비밀번호 유효한지 확인
    public boolean isTempPasswordValid(){
        return tempPassword != null
                && tempPasswordExpiresAt != null
                && LocalDateTime.now().isBefore(tempPasswordExpiresAt);
    }

    /// 임시 비밀번호 초기화
    public void clearTempPassword(){
        this.tempPassword = null;
        this.tempPasswordExpiresAt = null;
    }

    /// 비밀번호 변경
    public void changePassword(String encodedNewPassword){
        this.password = encodedNewPassword;
        this.clearTempPassword();
    }
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }


     /// 프로필 수정
    public void updateProfile(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }


    public void changeEmail(String email) {
        this.email = email;
    }

    /// 회원탈퇴
    public void withdraw(LocalDateTime withdrawnAt) {
        this.status = UserStatus.WITHDRAWN;
        this.withdrawnAt = withdrawnAt;
    }

    /// 탈퇴시각조회
    public LocalDateTime getWithdrawnAt() {
        return this.withdrawnAt;
    }

}