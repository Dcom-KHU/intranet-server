package com.dcom.intranet.mypage.dto;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "회원 탈퇴 응답 데이터")
public record MemberWithdrawResponse(
        @Schema(description = "회원 ID", example = "1")
        Long userId,

        @Schema(description = "회원 상태", example = "WITHDRAWN")
        UserStatus status,

        @Schema(description = "탈퇴 일시", example = "2026-07-01T10:30:00+09:00")
        LocalDateTime withdrawnAt
) {
    public static MemberWithdrawResponse from(User user) {
        return new MemberWithdrawResponse(user.getId(), user.getStatus(), user.getWithdrawnAt());
    }
}
