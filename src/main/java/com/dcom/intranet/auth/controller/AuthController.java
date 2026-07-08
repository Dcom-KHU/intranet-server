package com.dcom.intranet.auth.controller;

import com.dcom.intranet.auth.dto.auth.*;
import com.dcom.intranet.auth.service.AuthService;
import com.dcom.intranet.auth.service.EmailService;
import com.dcom.intranet.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "인증", description = "회원가입, 로그인, 토큰 관리 등 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String SIGNUP_SUCCESS_201_EXAMPLE = """
            {
              "userId": 1,
              "loginId": "dcom2024",
              "studentId": "20241234",
              "email": "dcom2024@example.com",
              "status": "PENDING"
            }
            """;

    private static final String SIGNUP_BAD_REQUEST_400_EXAMPLE = """
            {
              "success": false,
              "status": 400,
              "message": "이메일 인증이 완료되지 않았습니다.",
              "data": null
            }
            """;

    private static final String SIGNUP_CONFLICT_409_EXAMPLE = """
            {
              "success": false,
              "status": 409,
              "message": "이미 사용 중인 아이디입니다.",
              "data": null
            }
            """;

    private static final String CHECK_LOGIN_ID_SUCCESS_200_EXAMPLE = """
            {
              "isAvailable": true,
              "message": "사용가능한 아이디입니다."
            }
            """;

    private static final String LOGIN_SUCCESS_200_EXAMPLE = """
            {
              "accessToken": "eyJhbGciOiJIUzI1NiJ9.xxxxx.yyyyy",
              "refreshToken": "eyJhbGciOiJIUzI1NiJ9.aaaaa.bbbbb",
              "userId": 1,
              "role": "USER",
              "status": "APPROVED",
              "requirePasswordChange": false
            }
            """;

    private static final String LOGIN_UNAUTHORIZED_401_EXAMPLE = """
            {
              "success": false,
              "status": 401,
              "message": "아이디 또는 비밀번호가 올바르지 않습니다.",
              "data": null
            }
            """;

    private static final String LOGIN_FORBIDDEN_403_EXAMPLE = """
            {
              "success": false,
              "status": 403,
              "message": "승인되지 않은 회원입니다.",
              "data": null
            }
            """;

    private static final String ME_UNAUTHORIZED_401_EXAMPLE = """
            {
              "success": false,
              "status": 401,
              "message": "인증이 필요합니다.",
              "data": null
            }
            """;

    private static final String EMAIL_SEND_SUCCESS_200_EXAMPLE = """
            {
              "message": "인증코드가 발송되었습니다.",
              "expiresIn": 300
            }
            """;

    private static final String EMAIL_SEND_BAD_REQUEST_400_EXAMPLE = """
            {
              "success": false,
              "status": 400,
              "message": "이메일 형식이 올바르지 않습니다.",
              "data": null
            }
            """;

    private static final String EMAIL_VERIFY_SUCCESS_200_EXAMPLE = """
            {
              "message": "이메일 인증이 완료되었습니다.",
              "email": "dcom2024@example.com"
            }
            """;

    private static final String EMAIL_VERIFY_BAD_REQUEST_400_EXAMPLE = """
            {
              "success": false,
              "status": 400,
              "message": "인증 코드가 올바르지 않습니다.",
              "data": null
            }
            """;

    private static final String REFRESH_UNAUTHORIZED_401_EXAMPLE = """
            {
              "success": false,
              "status": 401,
              "message": "유효하지 않은 RefreshToken입니다.",
              "data": null
            }
            """;

    private static final String LOGOUT_SUCCESS_200_EXAMPLE = """
            {
              "message": "로그아웃 되었습니다."
            }
            """;

    private static final String PASSWORD_RESET_SEND_SUCCESS_200_EXAMPLE = """
            {
              "message": "임시 비밀번호가 발송되었습니다.",
              "expiresIn": 1800
            }
            """;

    private static final String PASSWORD_RESET_SEND_BAD_REQUEST_400_EXAMPLE = """
            {
              "success": false,
              "status": 400,
              "message": "해당 이메일로 가입된 회원이 없습니다.",
              "data": null
            }
            """;

    private static final String PASSWORD_RESET_SUCCESS_200_EXAMPLE = """
            {
              "message": "비밀번호가 변경되었습니다."
            }
            """;

    private final AuthService authService;
    private final EmailService emailService;


    @Operation(
            summary = "회원가입",
            description = """
                    회원가입을 요청합니다. 인증이 필요없는 API입니다.

                    - 아이디, 학번, 이메일은 각각 다른 회원과 중복될 수 없습니다. 중복 시 409를 반환합니다.
                    - 이메일은 도메인 제한이 없으며, 가입 전 /api/auth/email/send, /api/auth/email/verify로 인증을 완료해야 합니다. 인증되지 않은 이메일로 가입을 시도하면 400을 반환합니다.
                    - 가입 직후 상태는 항상 PENDING(관리자 승인 대기)이며, 관리자가 승인해야 로그인할 수 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SignupResponse.class),
                            examples = @ExampleObject(value = SIGNUP_SUCCESS_201_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "필수값 누락, 이메일 형식 오류 또는 이메일 인증 미완료",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SIGNUP_BAD_REQUEST_400_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "아이디, 학번 또는 이메일 중복",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SIGNUP_CONFLICT_409_EXAMPLE)
                    )
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        /// 201응답으로 해주기 위해서 GttpStatus.CREATED (201)
    }
    /// @Valid 어노테이션은 NotBlack와 Email이 검증이 자동으로 된다. 실패하면 400 Error. 따라서 if문 안써도 된다.

    @Operation(
            summary = "아이디 중복 확인",
            description = "입력한 로그인 아이디가 이미 사용 중인지 확인합니다. 인증이 필요없는 API이며, 중복되어도 에러가 아닌 200과 isAvailable=false로 응답합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "확인 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckLoginIdResponse.class),
                            examples = @ExampleObject(value = CHECK_LOGIN_ID_SUCCESS_200_EXAMPLE)
                    )
            )
    })
    @GetMapping("/check-login-id")
    public ResponseEntity<CheckLoginIdResponse> checkLoginId(
            @Parameter(description = "중복 확인할 로그인 아이디", example = "dcom2024")
            @RequestParam String loginId
    ) {
        CheckLoginIdResponse response = authService.checkLoginId(loginId);
        return ResponseEntity.ok(response);
    }
    /// Get은 Body가 아니라 쿼리 파라미터로 받아서 로그인 아이디에 있는 거랑 열이랑 비교해서 없으면 오케이

    @Operation(
            summary = "로그인",
            description = """
                    아이디와 비밀번호로 로그인합니다. 인증이 필요없는 API입니다.

                    - 정식 비밀번호 또는 발급 후 30분 이내인 임시 비밀번호로 로그인할 수 있습니다. 임시 비밀번호로 로그인에 성공하면 응답의 requirePasswordChange가 true가 되며, 이 경우 /api/auth/password로 새 비밀번호를 설정해야 합니다.
                    - 이메일 미인증 상태에서는 애초에 회원가입이 완료되지 않으므로 로그인 대상이 될 수 없습니다.
                    - 관리자 승인 대기(PENDING), 탈퇴(WITHDRAWN) 상태의 회원은 로그인이 차단되며 403을 반환합니다.
                    - 가입이 거절된 회원은 계정 자체가 삭제되므로, 아이디/비밀번호 불일치와 동일하게 401을 반환합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginResponse.class),
                            examples = @ExampleObject(value = LOGIN_SUCCESS_200_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "아이디 또는 비밀번호 불일치",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = LOGIN_UNAUTHORIZED_401_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "승인 대기 또는 탈퇴 상태로 로그인 차단",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = LOGIN_FORBIDDEN_403_EXAMPLE)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내 정보 조회",
            description = "Access Token으로 로그인 상태의 사용자 정보를 조회합니다. 앱 초기 진입 시 보유한 토큰이 유효한지 확인하는 용도로 사용합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MeResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요 (토큰 없음 또는 만료/위조된 토큰)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = ME_UNAUTHORIZED_401_EXAMPLE)
                    )
            )
    })
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(
            @Parameter(hidden = true) @AuthenticationPrincipal String loginId
    ) {
        MeResponse response = authService.me(loginId);
        return ResponseEntity.ok(response);
    }
    /// UsernamePasswordAuthenticationToken의 첫번째 인자가 principal인데 그게 로그인 아이디임.
    /// 토큰에 아이디를 넣어놓았기 때문에 요청한 사람이 누구인지 자동으로 알아내는 로직임.

    @Operation(
            summary = "이메일 인증코드 발송",
            description = "가입하려는 이메일로 6자리 인증코드를 발송합니다. 발급된 인증코드는 5분(300초)간 유효합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "발송 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = EMAIL_SEND_SUCCESS_200_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이메일 형식 오류",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = EMAIL_SEND_BAD_REQUEST_400_EXAMPLE)
                    )
            )
    })
    @PostMapping("/email/send")
    public ResponseEntity<Map<String, Object>> sendEmail(@Valid @RequestBody EmailSendRequest request) {
        emailService.sendVerificationCode(request.getEmail());
        Map<String, Object> response = Map.of(
                "message", "인증코드가 발송되었습니다.", "expiresIn", 300
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "이메일 인증코드 확인",
            description = "발송된 인증코드를 확인하여 이메일 인증을 완료합니다. 회원가입 이전 단계에서 호출되므로 아직 사용자 계정이 없고, 응답에도 별도의 인증 상태(state)나 userId는 포함되지 않습니다. 인증이 완료된 이메일은 /api/auth/signup 요청 시 이메일 인증 완료 여부 확인에 사용됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = EMAIL_VERIFY_SUCCESS_200_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "인증 요청 없음, 코드 만료, 이미 인증됨 또는 코드 불일치",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = EMAIL_VERIFY_BAD_REQUEST_400_EXAMPLE)
                    )
            )
    })
    @PostMapping("/email/verify")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
        emailService.verifyCode(request.getEmail(), request.getVerificationCode());

        Map<String, String> response = Map.of(
                "message", "이메일 인증이 완료되었습니다.", "email", request.getEmail()
        );
        /// 이렇게 Map of을 하면 간단한 것들은 그냥 DTO안만들고 바로 JSON만들어서 준다. 굳이 response 폴더를 만들 필요 없음
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "토큰 재발급",
            description = "Refresh Token으로 Access Token과 Refresh Token을 재발급받아 로그인 상태를 유지합니다. 재발급 시 기존 Refresh Token은 폐기되고 새 Refresh Token이 발급됩니다(Rotation)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "재발급 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RefreshResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh Token이 존재하지 않거나 만료됨",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = REFRESH_UNAUTHORIZED_401_EXAMPLE)
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse response = authService.refresh(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "로그아웃",
            description = "전달받은 Refresh Token을 DB에서 삭제하여 무효화합니다. 존재하지 않는 Refresh Token이 와도 에러 없이 200으로 응답합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = LOGOUT_SUCCESS_200_EXAMPLE)
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshRequest request){
        authService.logout(request);
        Map<String, String> response = Map.of("message", "로그아웃 되었습니다.");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "임시 비밀번호 발송",
            description = "가입된 이메일로 임시 비밀번호를 발송합니다. 발급된 임시 비밀번호는 30분(1800초)간 유효하며, 이 비밀번호로 로그인하면 /api/auth/login 응답의 requirePasswordChange가 true로 내려갑니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "발송 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = PASSWORD_RESET_SEND_SUCCESS_200_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "해당 이메일로 가입된 회원 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = PASSWORD_RESET_SEND_BAD_REQUEST_400_EXAMPLE)
                    )
            )
    })
    @PostMapping("/password/reset/send")
    public ResponseEntity<Map<String, Object>> sendTempPassword(@Valid @RequestBody EmailSendRequest request){
        authService.sendTempPassword(request.getEmail());

        Map<String, Object> response = Map.of("message", "임시 비밀번호가 발송되었습니다.", "expiresIn", 1800);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "비밀번호 재설정",
            description = "임시 비밀번호로 로그인한 뒤, 발급받은 Access Token으로 인증하여 새 비밀번호를 설정합니다. 비밀번호 변경이 완료되면 임시 비밀번호는 폐기됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "재설정 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = PASSWORD_RESET_SUCCESS_200_EXAMPLE)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요 (토큰 없음 또는 만료/위조된 토큰)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = ME_UNAUTHORIZED_401_EXAMPLE)
                    )
            )
    })
    @PostMapping("/password")
    public ResponseEntity<Map<String,String>> resetPassword(
            @Parameter(hidden = true) @AuthenticationPrincipal String loginId,
            @Valid @RequestBody PasswordResetRequest request){
        authService.resetPassword(loginId, request.getNewPassword());
        Map<String,String> response = Map.of("message", "비밀번호가 변경되었습니다.");
        return ResponseEntity.ok(response);
    }

}