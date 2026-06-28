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
import com.dcom.intranet.mypage.dto.GoneApiResponse;
import com.dcom.intranet.mypage.dto.MyProfileApiResponse;
import com.dcom.intranet.mypage.dto.MyProfileResponse;
import com.dcom.intranet.mypage.dto.TooManyRequestsApiResponse;
import com.dcom.intranet.mypage.dto.UnauthorizedApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
