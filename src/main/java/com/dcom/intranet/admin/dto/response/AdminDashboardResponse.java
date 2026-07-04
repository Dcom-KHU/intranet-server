package com.dcom.intranet.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AdminDashboardResponse(
        long pendingUserCount,
        long totalUserCount,
        List<SignupRequestSummary> recentSignupRequests,
        MemberSummary memberSummary,
        List<RecentContent> recentContents
) {

    public record SignupRequestSummary(
            Long userId,
            String name,
            String studentId,
            String email,
            LocalDateTime createdAt
    ) {
    }

    public record MemberSummary(
            long approvedCount,
            long pendingCount,
            long withdrawnCount
    ) {
    }

    public record RecentContent(
            String type,
            Long contentId,
            String title,
            LocalDateTime createdAt
    ) {
    }
}
