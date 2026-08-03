package com.dcom.intranet.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 회원 탈퇴/삭제 처리 응답")
public record AdminUserWithdrawResponse(
        @Schema(description = "처리 대상 회원 ID", example = "123")
        Long userId,

        @Schema(description = "처리 결과", example = "WITHDRAWN", allowableValues = {"WITHDRAWN", "HARD_DELETED"})
        String result,

        @Schema(description = "처리 결과 메시지", example = "활동 이력이 있어 탈퇴 상태로 변경되었습니다.")
        String message
) {

    public static AdminUserWithdrawResponse withdrawn(Long userId) {
        return new AdminUserWithdrawResponse(
                userId,
                "WITHDRAWN",
                "활동 이력이 있어 탈퇴 상태로 변경되었습니다."
        );
    }

    public static AdminUserWithdrawResponse hardDeleted(Long userId) {
        return new AdminUserWithdrawResponse(
                userId,
                "HARD_DELETED",
                "활동 이력이 없어 회원 정보가 삭제되었습니다."
        );
    }
}
