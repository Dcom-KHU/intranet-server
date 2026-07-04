package com.dcom.intranet.admin.dto.response;

import com.dcom.intranet.auth.domain.UserRole;

public record AdminMeResponse(
        Long userId,
        UserRole role,
        boolean accessible
) {
}
