package com.dcom.intranet.admin.dto.response;

import com.dcom.intranet.auth.domain.UserRole;

import java.time.LocalDateTime;

public record AdminTransferAdminResponse(
        Long previousAdminUserId,
        Long newAdminUserId,
        UserRole previousAdminRole,
        UserRole newAdminRole,
        LocalDateTime transferredAt
) {
}
