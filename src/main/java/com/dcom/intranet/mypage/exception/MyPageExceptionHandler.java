package com.dcom.intranet.mypage.exception;

import com.dcom.intranet.common.ApiResponse;
import com.dcom.intranet.mypage.controller.MyPageController;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = MyPageController.class)
public class MyPageExceptionHandler {

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
