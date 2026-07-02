package com.dcom.intranet.mypage;

import org.springframework.http.HttpStatus;

public class MyPageApiException extends RuntimeException {

    private final HttpStatus status;

    public MyPageApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
