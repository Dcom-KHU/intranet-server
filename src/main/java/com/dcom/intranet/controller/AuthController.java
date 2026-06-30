package com.dcom.intranet.controller;

import com.dcom.intranet.dto.auth.*;
import com.dcom.intranet.service.AuthService;
import com.dcom.intranet.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final EmailService emailService;


    /// 회원가입
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        /// 201응답으로 해주기 위해서 GttpStatus.CREATED (201)
    }
    /// @Valid 어노테이션은 NotBlack와 Email이 검증이 자동으로 된다. 실패하면 400 Error. 따라서 if문 안써도 된다.

    /// 아이디 중복 확인
    @GetMapping("/check-login-id")
    public ResponseEntity<CheckLoginIdResponse> checkLoginId(@RequestParam String loginId) {
        CheckLoginIdResponse response = authService.checkLoginId(loginId);
        return ResponseEntity.ok(response);
    }
    /// Get은 Body가 아니라 쿼리 파라미터로 받아서 로그인 아이디에 있는 거랑 열이랑 비교해서 없으면 오케이

    /// 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal String loginId) {
        MeResponse response = authService.me(loginId);
        return ResponseEntity.ok(response);
    }
    /// UsernamePasswordAuthenticationToken의 첫번째 인자가 principal인데 그게 로그인 아이디임.
    /// 토큰에 아이디를 넣어놓았기 때문에 요청한 사람이 누구인지 자동으로 알아내는 로직임.

    /// 이메일 인증코드 발송
    @PostMapping("/email/send")
    public ResponseEntity<Map<String, Object>> sendEmail(@Valid @RequestBody EmailSendRequest request) {
        emailService.sendVerificationCode(request.getEmail());
        Map<String, Object> response = Map.of(
                "message", "인증코드가 발송되었습니다.", "expiresIn", 300
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody EmailVerifyRequest request) {
        emailService.verifyCode(request.getEmail(), request.getVerificationCode());

        Map<String, String> response = Map.of(
                "message", "이메일 인증이 완료되었습니다.", "email", request.getEmail()
        );
        /// 이렇게 Map of을 하면 간단한 것들은 그냥 DTO안만들고 바로 JSON만들어서 준다. 굳이 response 폴더를 만들 필요 없음
        return ResponseEntity.ok(response);
    }
}