package com.dcom.intranet.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // 학번
    @Column(nullable = false, unique = true, length = 20)
    private String studentNumber;

    // 이메일
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 이름 또는 닉네임
    @Column(nullable = false, length = 50)
    private String nickname;

    // 권한: USER, ADMIN
    @Column(nullable = false, length = 20)
    private String role;

    public User(String studentNumber, String email, String nickname, String role) {
        this.studentNumber = studentNumber;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }
}