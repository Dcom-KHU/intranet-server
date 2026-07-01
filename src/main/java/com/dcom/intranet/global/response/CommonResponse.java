package com.dcom.intranet.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "공통 API 응답")
public class CommonResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    private final int status;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private final String message;

    @Schema(description = "응답 데이터")
    private final T data;

    private CommonResponse(boolean success, int status, String message, T data) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(
                true,
                200,
                "요청이 성공적으로 처리되었습니다.",
                data
        );
    }

    public static <T> CommonResponse<T> success(int status, String message, T data) {
        return new CommonResponse<>(
                true,
                status,
                message,
                data
        );
    }

    public static CommonResponse<Void> success(int status, String message) {
        return new CommonResponse<>(
                true,
                status,
                message,
                null
        );
    }

    public static CommonResponse<Void> fail(int status, String message) {
        return new CommonResponse<>(
                false,
                status,
                message,
                null
        );
    }
}