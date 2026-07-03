package com.dcom.intranet.mypage.dto.response;

import com.dcom.intranet.auth.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원정보 수정 응답 데이터")
public record MyProfileUpdateResponse(
        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "전체 학번", example = "2026123456")
        String studentId,

        @Schema(description = "이메일", example = "newhong@khu.ac.kr")
        String email,

        @Schema(description = "전화번호", example = "010-9999-8888")
        String phoneNumber,

        @Schema(description = "처리 메시지", example = "회원정보가 수정되었습니다.")
        String message
) {

    public static MyProfileUpdateResponse from(User user) {
        return new MyProfileUpdateResponse(
                user.getName(),
                user.getStudentId(),
                user.getEmail(),
                user.getPhoneNumber(),
                "회원정보가 수정되었습니다."
        );
    }
}
