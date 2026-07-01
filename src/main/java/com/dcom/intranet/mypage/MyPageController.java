package com.dcom.intranet.mypage;

import com.dcom.intranet.common.ApiResponse;
import com.dcom.intranet.mypage.dto.BadRequestApiResponse;
import com.dcom.intranet.mypage.dto.ConflictApiResponse;
import com.dcom.intranet.mypage.dto.EmailVerificationSendApiResponse;
import com.dcom.intranet.mypage.dto.EmailVerificationSendRequest;
import com.dcom.intranet.mypage.dto.EmailVerificationSendResponse;
import com.dcom.intranet.mypage.dto.EmailVerificationVerifyApiResponse;
import com.dcom.intranet.mypage.dto.EmailVerificationVerifyRequest;
import com.dcom.intranet.mypage.dto.EmailVerificationVerifyResponse;
import com.dcom.intranet.mypage.dto.ForbiddenApiResponse;
import com.dcom.intranet.mypage.dto.GoneApiResponse;
import com.dcom.intranet.mypage.dto.MemberWithdrawApiResponse;
import com.dcom.intranet.mypage.dto.MemberWithdrawResponse;
import com.dcom.intranet.mypage.dto.MyProfileApiResponse;
import com.dcom.intranet.mypage.dto.MyProfileResponse;
import com.dcom.intranet.mypage.dto.MyProfileUpdateApiResponse;
import com.dcom.intranet.mypage.dto.MyProfileUpdateRequest;
import com.dcom.intranet.mypage.dto.MyProfileUpdateResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentListApiResponse;
import com.dcom.intranet.mypage.dto.MyWrittenCommentListResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostDeleteApiResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostDeleteResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostListApiResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostListResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostTargetApiResponse;
import com.dcom.intranet.mypage.dto.MyWrittenPostTargetResponse;
import com.dcom.intranet.mypage.dto.NotFoundApiResponse;
import com.dcom.intranet.mypage.dto.PasswordChangeApiResponse;
import com.dcom.intranet.mypage.dto.PasswordChangeRequest;
import com.dcom.intranet.mypage.dto.PasswordChangeResponse;
import com.dcom.intranet.mypage.dto.TooManyRequestsApiResponse;
import com.dcom.intranet.mypage.dto.UnauthorizedApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class MyPageController {

    private final MyPageService myPageService;
    private final EmailVerificationService emailVerificationService;

    public MyPageController(MyPageService myPageService, EmailVerificationService emailVerificationService) {
        this.myPageService = myPageService;
        this.emailVerificationService = emailVerificationService;
    }

    @Operation(
            summary = "회원정보 조회",
            description = "인증된 사용자의 회원정보를 조회한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "회원정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = MyProfileApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    )
            }
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile(Authentication authentication) {
        MyProfileResponse response = myPageService.getMyProfile(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "내가 쓴 글 목록 조회",
            description = "인증된 사용자가 본인이 작성한 정보 공유 게시글, 족보 글, 활동 사진 댓글 목록을 조회한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "내가 쓴 글 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = MyWrittenPostListApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    )
            }
    )
    @GetMapping("/me/posts")
    public ResponseEntity<ApiResponse<MyWrittenPostListResponse>> getMyPosts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type
    ) {
        MyWrittenPostListResponse response = myPageService.getMyPosts(
                authentication.getName(),
                page,
                size,
                type
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "내가 쓴 댓글 목록 조회",
            description = "인증된 사용자가 본인이 작성한 정보 공유 게시글, 활동 사진 댓글 목록을 조회한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "내가 쓴 댓글 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = MyWrittenCommentListApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    )
            }
    )
    @GetMapping("/me/comments")
    public ResponseEntity<ApiResponse<MyWrittenCommentListResponse>> getMyComments(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type
    ) {
        MyWrittenCommentListResponse response = myPageService.getMyComments(
                authentication.getName(),
                page,
                size,
                type
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "내가 쓴 글 상세 이동",
            description = "인증된 사용자가 본인이 작성한 글을 선택하면 상세 페이지 이동 대상 정보를 반환한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "내가 쓴 글 상세 이동 대상 조회 성공",
                            content = @Content(schema = @Schema(implementation = MyWrittenPostTargetApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "작성한 글 없음",
                            content = @Content(schema = @Schema(implementation = NotFoundApiResponse.class))
                    )
            }
    )
    @GetMapping("/me/posts/{postId}")
    public ResponseEntity<ApiResponse<MyWrittenPostTargetResponse>> getMyPostTarget(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestParam String type
    ) {
        MyWrittenPostTargetResponse response = myPageService.getMyPostTarget(
                authentication.getName(),
                postId,
                type
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "내가 쓴 글 삭제",
            description = "인증된 사용자가 본인이 작성한 글을 삭제한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "내가 쓴 글 삭제 성공",
                            content = @Content(schema = @Schema(implementation = MyWrittenPostDeleteApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "403",
                            description = "삭제 권한 없음",
                            content = @Content(schema = @Schema(implementation = ForbiddenApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "작성한 글 없음",
                            content = @Content(schema = @Schema(implementation = NotFoundApiResponse.class))
                    )
            }
    )
    @DeleteMapping("/me/posts/{postId}")
    public ResponseEntity<ApiResponse<MyWrittenPostDeleteResponse>> deleteMyPost(
            Authentication authentication,
            @PathVariable Long postId,
            @RequestParam String type
    ) {
        MyWrittenPostDeleteResponse response = myPageService.deleteMyPost(
                authentication.getName(),
                postId,
                type
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "이메일 변경 인증 메일 발송",
            description = "새 이메일에 대한 변경 인증 코드를 생성한다. 실제 메일 발송은 추후 메일 서비스 연동 시 연결한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "이메일 변경 인증 코드 생성 성공",
                            content = @Content(schema = @Schema(implementation = EmailVerificationSendApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "요청값 오류",
                            content = @Content(schema = @Schema(implementation = BadRequestApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "이미 사용 중인 이메일",
                            content = @Content(schema = @Schema(implementation = ConflictApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "429",
                            description = "진행 중인 인증 요청 존재",
                            content = @Content(schema = @Schema(implementation = TooManyRequestsApiResponse.class))
                    )
            }
    )
    @PostMapping("/me/email/verification/send")
    public ResponseEntity<ApiResponse<EmailVerificationSendResponse>> sendEmailVerification(
            Authentication authentication,
            @Valid @RequestBody EmailVerificationSendRequest request
    ) {
        EmailVerificationSendResponse response =
                emailVerificationService.sendEmailChangeVerification(authentication.getName(), request.newEmail());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "이메일 변경 인증 확인",
            description = "새 이메일과 인증 코드를 검증하고 회원정보 수정에서 사용할 이메일 변경 토큰을 발급한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "이메일 변경 인증 성공",
                            content = @Content(schema = @Schema(implementation = EmailVerificationVerifyApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "요청값 오류 또는 인증 코드 불일치",
                            content = @Content(schema = @Schema(implementation = BadRequestApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "410",
                            description = "인증 코드 만료",
                            content = @Content(schema = @Schema(implementation = GoneApiResponse.class))
                    )
            }
    )
    @PostMapping("/me/email/verification/verify")
    public ResponseEntity<ApiResponse<EmailVerificationVerifyResponse>> verifyEmailVerification(
            Authentication authentication,
            @Valid @RequestBody EmailVerificationVerifyRequest request
    ) {
        EmailVerificationVerifyResponse response = emailVerificationService.verifyEmailChangeCode(
                authentication.getName(),
                request.newEmail(),
                request.verificationCode()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "회원정보 수정",
            description = "인증된 사용자의 이름, 전화번호를 수정하고 검증된 이메일 변경 토큰이 있으면 이메일도 변경한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "회원정보 수정 성공",
                            content = @Content(schema = @Schema(implementation = MyProfileUpdateApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "요청값 오류 또는 이메일 변경 토큰 오류",
                            content = @Content(schema = @Schema(implementation = BadRequestApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "이미 사용 중인 이메일",
                            content = @Content(schema = @Schema(implementation = ConflictApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "410",
                            description = "이메일 변경 토큰 만료",
                            content = @Content(schema = @Schema(implementation = GoneApiResponse.class))
                    )
            }
    )
    @PatchMapping("/me/settings")
    public ResponseEntity<ApiResponse<MyProfileUpdateResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody MyProfileUpdateRequest request
    ) {
        MyProfileUpdateResponse response = myPageService.updateMyProfile(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "비밀번호 변경",
            description = "인증된 사용자의 현재 비밀번호를 확인한 뒤 새 비밀번호로 변경한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "비밀번호 변경 성공",
                            content = @Content(schema = @Schema(implementation = PasswordChangeApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "요청값 오류 또는 현재 비밀번호 불일치",
                            content = @Content(schema = @Schema(implementation = BadRequestApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    )
            }
    )
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<PasswordChangeResponse>> changePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        PasswordChangeResponse response = myPageService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "인증된 사용자의 회원 상태를 WITHDRAWN으로 변경한다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "회원 탈퇴 성공",
                            content = @Content(schema = @Schema(implementation = MemberWithdrawApiResponse.class))
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(schema = @Schema(implementation = UnauthorizedApiResponse.class))
                    )
            }
    )
    @PatchMapping("/me/withdraw")
    public ResponseEntity<ApiResponse<MemberWithdrawResponse>> withdraw(Authentication authentication) {
        MemberWithdrawResponse response = myPageService.withdraw(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
