package com.dcom.intranet.mypage.dto.response;

import com.dcom.intranet.auth.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원정보 조회 응답 데이터")
public record MyProfileResponse(
        @Schema(description = "로그인 ID", example = "hongil123")
        String loginId,

        @Schema(description = "이메일", example = "honghong@khu.ac.kr")
        String email,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "전화번호", example = "010-1234-4567")
        String phoneNumber,

        @Schema(description = "전체 학번", example = "2026123456")
        String studentId,

        @Schema(description = "유효한 임시 비밀번호가 있어 비밀번호 변경이 필요한지 여부", example = "false")
        boolean requirePasswordChange
) {

    public static MyProfileResponse from(User user) {
        return new MyProfileResponse(
                user.getLoginId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getStudentId(),
                user.isTempPasswordValid()
        );
    }
}
