package com.dcom.intranet.admin.dto.response;

import com.dcom.intranet.auth.domain.UserStatus;

import java.time.LocalDateTime;

public record AdminUserApproveResponse(
        Long userId,
        UserStatus status,
        LocalDateTime approvedAt,
        Long approvedByAdminId
) {
}
