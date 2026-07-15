package com.dcom.intranet.admin.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "관리자 대시보드 응답")
public record AdminDashboardResponse(
        @Schema(description = "승인 대기 회원 수", example = "3")
        long pendingUserCount,

        @Schema(description = "전체 회원 수", example = "128")
        long totalUserCount,

        @Schema(description = "최근 가입 신청 목록 (최대 5명, 최신순)")
        List<SignupRequestSummary> recentSignupRequests,

        @Schema(description = "최근 접속 회원 목록 (최대 3명, 최신 접속일순)")
        List<RecentActiveMember> recentActiveMembers,

        @Schema(description = "게시판별 게시글 개수")
        PostCounts postCounts
) {

    @Schema(description = "가입 신청 요약")
    public record SignupRequestSummary(
            @Schema(description = "회원 ID", example = "1")
            Long userId,

            @Schema(description = "이름", example = "하성준")
            String name,

            @Schema(description = "학번", example = "20230001")
            String studentId,

            @Schema(description = "이메일", example = "example@khu.ac.kr")
            String email,

            @Schema(description = "가입 신청일")
            LocalDateTime createdAt
    ) {
    }

    @Schema(description = "최근 접속 회원 요약")
    public record RecentActiveMember(
            @Schema(description = "회원 ID", example = "1")
            Long userId,

            @Schema(description = "이름", example = "하성준")
            String name,

            @Schema(description = "학번", example = "20230001")
            String studentId,

            @Schema(description = "최근 접속일")
            LocalDateTime lastLoginAt
    ) {
    }

    @Schema(description = "게시판별 게시글 개수")
    public record PostCounts(
            @Schema(description = "공지사항 개수", example = "12")
            long noticeCount,

            @Schema(description = "족보 개수", example = "45")
            long archiveCount,

            @Schema(description = "정보공유 게시글 개수", example = "30")
            long infoPostCount,

            @Schema(description = "사진첩 개수", example = "8")
            long photoPostCount
    ) {
    }
}
