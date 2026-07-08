package com.dcom.intranet.global.dto;

import com.dcom.intranet.auth.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "작성자 요약")
public record AuthorResponse(
        @Schema(description = "학번", example = "20201234")
        String studentNumber,

        @Schema(description = "이름", example = "표지훈")
        String name
) {

    public static AuthorResponse from(User user) {
        return new AuthorResponse(
                user.getStudentId(),
                user.getName()
        );
    }
}
