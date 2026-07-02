package com.dcom.intranet.common;

import com.dcom.intranet.mypage.MyPageApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(400, "요청값이 올바르지 않습니다."));
    }

    @ExceptionHandler(MyPageApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleMyPageApiException(MyPageApiException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(ApiResponse.failure(exception.getStatus().value(), exception.getMessage()));
    }
}
