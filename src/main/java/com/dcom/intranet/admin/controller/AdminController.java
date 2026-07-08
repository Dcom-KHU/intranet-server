package com.dcom.intranet.admin.controller;

import com.dcom.intranet.admin.dto.request.AdminTransferAdminRequest;
import com.dcom.intranet.admin.dto.response.AdminDashboardResponse;
import com.dcom.intranet.admin.dto.response.AdminMeResponse;
import com.dcom.intranet.admin.dto.response.AdminPendingUserListResponse;
import com.dcom.intranet.admin.dto.response.AdminTransferAdminResponse;
import com.dcom.intranet.admin.dto.response.AdminUserApproveResponse;
import com.dcom.intranet.admin.dto.response.AdminUserDetailResponse;
import com.dcom.intranet.admin.dto.response.AdminUserListResponse;
import com.dcom.intranet.admin.dto.response.AdminUserRejectResponse;
import com.dcom.intranet.admin.service.AdminService;
import com.dcom.intranet.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자", description = "관리자 콘솔 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "관리자 콘솔 접근 확인", description = "관리자 콘솔에 접근 가능한지 확인합니다.")
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<AdminMeResponse>> me(@AuthenticationPrincipal String loginId) {
        return ResponseEntity.ok(CommonResponse.success(adminService.me(loginId)));
    }

    @Operation(summary = "관리자 대시보드 조회", description = "승인 대기 수, 전체 회원 수 등 요약 정보를 조회합니다.")
    @GetMapping("/dashboard")
    public ResponseEntity<CommonResponse<AdminDashboardResponse>> getDashboard() {
        return ResponseEntity.ok(CommonResponse.success(adminService.getDashboard()));
    }

    @Operation(summary = "회원 목록 조회", description = "학번, 최근 접속일 기준 정렬을 지원하는 전체 회원 목록을 조회합니다.")
    @GetMapping("/users")
    public ResponseEntity<CommonResponse<AdminUserListResponse>> getUserList(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(CommonResponse.success(adminService.getUserList(keyword, pageable)));
    }

    @Operation(summary = "가입 승인 대상 조회", description = "PENDING 상태인 회원 목록을 조회합니다.")
    @GetMapping("/users/pending")
    public ResponseEntity<CommonResponse<AdminPendingUserListResponse>> getPendingUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(CommonResponse.success(adminService.getPendingUsers(pageable)));
    }

    @Operation(summary = "회원 상세 조회", description = "관리자용 회원 상세 정보를 조회합니다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<CommonResponse<AdminUserDetailResponse>> getUserDetail(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(CommonResponse.success(adminService.getUserDetail(userId)));
    }

    @Operation(summary = "가입 승인", description = "PENDING 회원을 승인 처리하고 승인 안내 메일을 발송합니다.")
    @PatchMapping("/users/{userId}/approve")
    public ResponseEntity<CommonResponse<AdminUserApproveResponse>> approveUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal String loginId
    ) {
        return ResponseEntity.ok(CommonResponse.success(adminService.approveUser(userId, loginId)));
    }

    @Operation(summary = "가입 거절", description = "PENDING 회원을 거절합니다. 거절된 회원 정보는 DB에서 물리 삭제됩니다.")
    @PatchMapping("/users/{userId}/reject")
    public ResponseEntity<CommonResponse<AdminUserRejectResponse>> rejectUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal String loginId
    ) {
        return ResponseEntity.ok(CommonResponse.success(adminService.rejectUser(userId, loginId)));
    }

    @Operation(
            summary = "관리자 권한 이양",
            description = "path의 userId(기존 관리자 본인)가 targetUserId에게 관리자 권한을 이양합니다. 기존 관리자는 USER로 전환됩니다."
    )
    @PatchMapping("/users/{userId}/transfer-admin")
    public ResponseEntity<CommonResponse<AdminTransferAdminResponse>> transferAdmin(
            @PathVariable Long userId,
            @Valid @RequestBody AdminTransferAdminRequest request,
            @AuthenticationPrincipal String loginId
    ) {
        return ResponseEntity.ok(CommonResponse.success(adminService.transferAdmin(userId, loginId, request)));
    }
}
