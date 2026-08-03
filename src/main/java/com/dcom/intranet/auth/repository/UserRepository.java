package com.dcom.intranet.auth.repository;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByEmail(String email);

    boolean existsByLoginId(String loginId);
    boolean existsByStudentId(String studentId);
    boolean existsByEmail(String email);

    Page<User> findByStatus(UserStatus status, Pageable pageable);
    List<User> findTop5ByStatusOrderByCreatedAtDesc(UserStatus status);
    List<User> findTop3ByStatusOrderByLastLoginAtDesc(UserStatus status);
    long countByStatus(UserStatus status);

    Page<User> findByNameContainingOrLoginIdContainingOrStudentIdContaining(
            String name, String loginId, String studentId, Pageable pageable
    );

    @Query("""
            SELECT u FROM User u
            WHERE u.status = :status
              AND (
                  u.name LIKE CONCAT('%', :keyword, '%')
                  OR u.loginId LIKE CONCAT('%', :keyword, '%')
                  OR u.studentId LIKE CONCAT('%', :keyword, '%')
              )
            """)
    Page<User> findByStatusAndKeyword(
            @Param("status") UserStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

}
