package com.dcom.intranet.repository;

import com.dcom.intranet.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);
    boolean existsByStudentId(String studentId);
    boolean existsByEmail(String email);
}
