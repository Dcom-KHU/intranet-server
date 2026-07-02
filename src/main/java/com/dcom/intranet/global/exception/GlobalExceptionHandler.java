package com.dcom.intranet.global.exception;

import com.dcom.intranet.global.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ===== 커스텀 예외 =====

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CommonResponse<Void>> handleBadRequest(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.fail(400, e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<CommonResponse<Void>> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponse.fail(401, e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<CommonResponse<Void>> handleConflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(CommonResponse.fail(409, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CommonResponse<Void>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(CommonResponse.fail(403, e.getMessage()));
    }

    // ===== 스프링 표준 예외 =====

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<CommonResponse<Void>> handleResponseStatusException(ResponseStatusException e) {
        int status = e.getStatusCode().value();
        return ResponseEntity.status(e.getStatusCode())
                .body(CommonResponse.fail(status, e.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("요청 값이 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.fail(400, message));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<CommonResponse<Void>> handleMissingRequestPart(MissingServletRequestPartException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.fail(400, "필수 요청 파트가 누락되었습니다: " + e.getRequestPartName()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonResponse<Void>> handleMissingRequestParameter(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.fail(400, "필수 요청 파라미터가 누락되었습니다: " + e.getParameterName()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CommonResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(CommonResponse.fail(413, "업로드 가능한 파일 크기를 초과했습니다."));
    }

    // ===== 최후의 보루 =====

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.fail(500, "서버 내부 오류가 발생했습니다."));
    }
}