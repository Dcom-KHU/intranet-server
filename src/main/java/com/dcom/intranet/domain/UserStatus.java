package com.dcom.intranet.domain;

public enum UserStatus {
    EMAIL_UNVERTIFIED, // 이메일 인증 전
    PENDING,    // 가입완료
    APPROVED,   // 승인됨 (로그인 가능)
    REJECTED,   // 거절됨
    WITHDRAWN,   // 탈퇴
}
