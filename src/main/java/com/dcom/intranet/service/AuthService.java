package com.dcom.intranet.service;

import com.dcom.intranet.domain.RefreshToken;
import com.dcom.intranet.domain.User;
import com.dcom.intranet.domain.UserStatus;
import com.dcom.intranet.dto.auth.*;
import com.dcom.intranet.jwt.JwtTokenProvider;
import com.dcom.intranet.repository.RefreshTokenRepository;
import com.dcom.intranet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;


    /// 회원가입
    @Transactional
    public SignupResponse signup(SignupRequest request){
        /// 아이디, 학번, 이메일 중복체크
        if (userRepository.existsByLoginId(request.getLoginId())){
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByLoginId(request.getStudentId())){
            throw new IllegalArgumentException("이미 가입된 학번입니다.");
        }
        if (userRepository.existsByLoginId(request.getEmail())){
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        /// 이메일 인증안됐으면 예외처리
        if(!emailService.isEmailVerified(request.getEmail())){
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }

        /// 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        /// User 생성
        User user = new User(
                request.getLoginId(),
                encodedPassword,
                request.getName(),
                request.getStudentId(),
                request.getEmail(),
                request.getPhoneNumber()
        );

        /// 저장
        User saved = userRepository.save(user);

        return SignupResponse.from(saved);

    }
    /// 아이디 중복 확인
    @Transactional
    public CheckLoginIdResponse checkLoginId(String loginId){
        boolean exists = userRepository.existsByLoginId(loginId);
        return CheckLoginIdResponse.of(!exists);
    }

    /// 로그인
    @Transactional
    public LoginResponse login(LoginRequest request){
        ///
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        /// 비밀번호 확인
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        /// status 확인
        if(user.getStatus() != UserStatus.APPROVED){
            throw new IllegalArgumentException("승인되지 않은 회원입니다.");
        }

        /// 최근 로그인 시각 갱신
        user.updateLastLoginAt();

        /// 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getLoginId(), user.getRole().name()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(
                user.getLoginId(), user.getRole().name()
        );

        /// Refresh Token DB에 저장
        refreshTokenRepository.save(
                new RefreshToken(refreshToken, user.getLoginId(), 1209600000L)
        );


        return LoginResponse.of(user, accessToken, refreshToken);

    }


    /// 로그인 상태 확인
    public MeResponse me(String loginId){
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        return MeResponse.from(user);
    }

    /// 토큰 재발급
    @Transactional
    public RefreshResponse refresh(RefreshRequest request){
        /// DB에서 refreshToken 찾기
        RefreshToken savedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(()-> new IllegalArgumentException("유효하지 않은 RefreshToken입니다."));

        /// 만료 확인
        if(savedToken.isExpired()){
            refreshTokenRepository.delete(savedToken);
            throw new IllegalArgumentException("로그인이 만료되었습니다. 다시 로그인해주세요.");
        }

        /// 토큰에서 정보 추출
        String loginId = jwtTokenProvider.getLoginId(request.getRefreshToken());
        String role = jwtTokenProvider.getRole(request.getRefreshToken());

        /// 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(loginId, role);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(loginId, role);


        /// 기존 리프레시 토큰 삭제 + 새거 저장(Rotation)
        refreshTokenRepository.delete(savedToken);
        refreshTokenRepository.save(new RefreshToken(newRefreshToken, loginId, 1209600000L));

        return RefreshResponse.of(newAccessToken, newRefreshToken, 1800);

    }

    @Transactional
    public void logout(RefreshRequest request){
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(refreshTokenRepository::delete);

    }






}
