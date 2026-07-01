package com.dcom.intranet.mypage.dto;

import com.dcom.intranet.mypage.MyPageRouteType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내가 쓴 글 상세 이동 응답 데이터")
public record MyWrittenPostTargetResponse(
        @Schema(description = "상세 페이지 이동 대상 URL segment", example = "archives")
        String targetType,

        @Schema(description = "상세 페이지 이동 대상 ID", example = "1")
        Long targetId
) {
    public MyWrittenPostTargetResponse {
        targetType = MyPageRouteType.normalize(targetType);
    }
}
