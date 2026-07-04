package com.dcom.intranet.admin.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdminTransferAdminRequest(
        @NotNull(message = "confirm 값은 필수입니다.")
        Boolean confirm,

        @NotNull(message = "targetUserId는 필수입니다.")
        Long targetUserId
) {
}
