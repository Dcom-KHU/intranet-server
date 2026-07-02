package com.dcom.intranet.auth.domain;

public enum UserStatus {
    PENDING,    // 가입전!
    APPROVED,   // 승인됨 (로그인 가능)!
    WITHDRAWN,   // 탈퇴!
}
