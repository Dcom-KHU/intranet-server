package com.dcom.intranet.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_student_number", columnNames = "student_number"),
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "student_number", nullable = false, length = 20)
    private String studentNumber;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String role;

    public User(String studentNumber, String email, String nickname, String role) {
        this.studentNumber = studentNumber;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(this.role);
    }
}