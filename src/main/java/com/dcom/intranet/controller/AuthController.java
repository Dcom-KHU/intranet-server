package com.dcom.intranet.controller;

import com.dcom.intranet.dto.auth.*;
import com.dcom.intranet.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /// 회원가입
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request){
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        /// 201응답으로 해주기 위해서 GttpStatus.CREATED (201)
    }
    /// @Valid 어노테이션은 NotBlack와 Email이 검증이 자동으로 된다. 실패하면 400 Error. 따라서 if문 안써도 된다.

    /// 아이디 중복 확인
    @GetMapping("/check-login-id")
    public ResponseEntity<CheckLoginIdResponse> checkLoginId(@RequestParam String loginId){
        CheckLoginIdResponse response = authService.checkLoginId(loginId);
        return ResponseEntity.ok(response);
    }
    /// Get은 Body가 아니라 쿼리 파라미터로 받아서 로그인 아이디에 있는 거랑 열이랑 비교해서 없으면 오케이

    /// 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
