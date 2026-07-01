package com.dcom.intranet.global.exception;

import com.dcom.intranet.global.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<CommonResponse<Void>> handleResponseStatusException(
            ResponseStatusException e
    ) {
        HttpStatusCode statusCode = e.getStatusCode();

        String message = e.getReason() != null
                ? e.getReason()
                : "요청 처리 중 오류가 발생했습니다.";

        return ResponseEntity
                .status(statusCode)
                .body(CommonResponse.fail(statusCode.value(), message));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<CommonResponse<Void>> handleMissingServletRequestPartException(
            MissingServletRequestPartException e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.fail(
                        400,
                        "필수 요청 파트가 누락되었습니다: " + e.getRequestPartName()
                ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<CommonResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.fail(
                        400,
                        "필수 요청 파라미터가 누락되었습니다: " + e.getParameterName()
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<CommonResponse<Void>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.fail(
                        400,
                        "업로드 가능한 파일 크기를 초과했습니다."
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.fail(
                        500,
                        "서버 내부 오류가 발생했습니다."
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("잘못된 요청입니다.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.fail(400, message));
    }
}