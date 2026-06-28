package com.dcom.intranet.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 API 응답")
public class ApiResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "HTTP 상태 코드", example = "200")
    private final int status;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private final String message;

    @Schema(description = "응답 데이터")
    private final T data;

    public ApiResponse(boolean success, int status, String message, T data) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, "요청이 성공적으로 처리되었습니다.", data);
    }

    public static ApiResponse<Void> failure(int status, String message) {
        return new ApiResponse<>(false, status, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
