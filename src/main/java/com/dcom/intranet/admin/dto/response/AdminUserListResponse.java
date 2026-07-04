package com.dcom.intranet.admin.dto.response;

import com.dcom.intranet.auth.domain.UserRole;
import com.dcom.intranet.auth.domain.UserStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserListResponse(
        List<UserSummary> userList,
        AdminPageInfo pageInfo
) {

    public static AdminUserListResponse from(Page<UserSummary> page) {
        return new AdminUserListResponse(page.getContent(), AdminPageInfo.from(page));
    }

    public record UserSummary(
            Long userId,
            String loginId,
            String name,
            String studentId,
            String email,
            UserRole role,
            UserStatus status,
            LocalDateTime lastLoginAt
    ) {
    }
}
