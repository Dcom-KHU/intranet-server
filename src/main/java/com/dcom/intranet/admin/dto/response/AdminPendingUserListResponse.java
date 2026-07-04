package com.dcom.intranet.admin.dto.response;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public record AdminPendingUserListResponse(
        List<PendingUserSummary> pendingUserList,
        AdminPageInfo pageInfo
) {

    public static AdminPendingUserListResponse from(Page<PendingUserSummary> page) {
        return new AdminPendingUserListResponse(page.getContent(), AdminPageInfo.from(page));
    }

    public record PendingUserSummary(
            Long userId,
            String loginId,
            String name,
            String studentId,
            String email,
            String phoneNumber,
            LocalDateTime createdAt
    ) {
    }
}
