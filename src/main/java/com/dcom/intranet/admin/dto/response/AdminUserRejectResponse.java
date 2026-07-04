package com.dcom.intranet.admin.dto.response;

import java.time.LocalDateTime;

public record AdminUserRejectResponse(
        Long userId,
        String status,
        Long rejectedByAdminId,
        LocalDateTime rejectedAt
) {
}
