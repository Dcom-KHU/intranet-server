package com.dcom.intranet.mypage.dto.response;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "회원 탈퇴 응답 데이터")
public record MemberWithdrawResponse(
        @Schema(description = "회원 ID", example = "1")
        Long userId,

        @Schema(description = "처리 결과", example = "WITHDRAWN", allowableValues = {"WITHDRAWN", "HARD_DELETED"})
        String result,

        @Schema(description = "회원 상태. 물리 삭제된 경우 null", example = "WITHDRAWN")
        UserStatus status,

        @Schema(description = "탈퇴 일시. 물리 삭제된 경우 null", example = "2026-07-01T10:30:00+09:00")
        LocalDateTime withdrawnAt,

        @Schema(description = "처리 결과 메시지", example = "활동 이력이 있어 탈퇴 상태로 변경되었습니다.")
        String message
) {
    public static MemberWithdrawResponse withdrawn(User user) {
        return new MemberWithdrawResponse(
                user.getId(),
                "WITHDRAWN",
                user.getStatus(),
                user.getWithdrawnAt(),
                "활동 이력이 있어 탈퇴 상태로 변경되었습니다."
        );
    }

    public static MemberWithdrawResponse hardDeleted(Long userId) {
        return new MemberWithdrawResponse(
                userId,
                "HARD_DELETED",
                null,
                null,
                "활동 이력이 없어 회원 정보가 삭제되었습니다."
        );
    }
}
