package com.dcom.intranet.admin.dto.response;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserRole;
import com.dcom.intranet.auth.domain.UserStatus;

import java.time.LocalDateTime;

public record AdminUserDetailResponse(
        Long userId,
        String loginId,
        String name,
        String studentId,
        String email,
        String phoneNumber,
        UserRole role,
        UserStatus status,
        LocalDateTime lastLoginAt
) {

    public static AdminUserDetailResponse from(User user) {
        return new AdminUserDetailResponse(
                user.getId(),
                user.getLoginId(),
                user.getName(),
                user.getStudentId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getStatus(),
                user.getLastLoginAt()
        );
    }
}
