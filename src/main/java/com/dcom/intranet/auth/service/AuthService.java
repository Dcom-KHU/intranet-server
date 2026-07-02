package com.dcom.intranet.auth.service;

import com.dcom.intranet.auth.dto.auth.*;
import com.dcom.intranet.auth.domain.RefreshToken;
import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.domain.UserStatus;
import com.dcom.intranet.global.exception.BadRequestException;
import com.dcom.intranet.global.exception.ConflictException;
import com.dcom.intranet.global.exception.UnauthorizedException;
import com.dcom.intranet.jwt.JwtTokenProvider;
import com.dcom.intranet.auth.repository.RefreshTokenRepository;
import com.dcom.intranet.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

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
            throw new ConflictException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByLoginId(request.getStudentId())){
            throw new ConflictException("이미 가입된 학번입니다.");
        }
        if (userRepository.existsByLoginId(request.getEmail())){
            throw new ConflictException("이미 사용 중인 이메일입니다.");
        }

        /// 이메일 인증안됐으면 예외처리
        if(!emailService.isEmailVerified(request.getEmail())){
            throw new BadRequestException("이메일 인증이 완료되지 않았습니다.");
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
    public LoginResponse login(LoginRequest request) {
        // TODO: 삭제할 것 - 테스트용 해시 출력
        System.out.println("HASH: " + passwordEncoder.encode("admin123"));

        /// 회원찾기
        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다."));

        /// 비밀번호 확인
        boolean usedTempPassword = false;

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            /// 기존 비밀번호로 로그인
            usedTempPassword = false;
        } else if (user.isTempPasswordValid() && passwordEncoder.matches(request.getPassword(), user.getTempPassword())){
            /// 임시 비밀번호로 로그인
            usedTempPassword = true;
        } else{
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }


        /// status 확인
        if(user.getStatus() != UserStatus.APPROVED){
            throw new IllegalStateException("승인되지 않은 회원입니다.");
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


        return LoginResponse.of(user, accessToken, refreshToken, usedTempPassword);

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
                .orElseThrow(()-> new UnauthorizedException("유효하지 않은 RefreshToken입니다."));

        /// 만료 확인
        if(savedToken.isExpired()){
            refreshTokenRepository.delete(savedToken);
            throw new UnauthorizedException("로그인이 만료되었습니다. 다시 로그인해주세요.");
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

    private static final int TEMP_PASSWORD_LENGTH = 8;
    private static final int TEMP_PASSWORD_EXPIRATION_MINUTES = 30;

    @Transactional
    public void sendTempPassword(String email){
        /// 이메일로 회원찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("해당 이메일로 가입된 회원이 없습니다."));

        /// 임시 비밀번호 생성
        String tempPassword = generateTempPassword();

        /// 비밀번호 암호화 후에 DB 저장
        String encodedTempPassword = passwordEncoder.encode(tempPassword);
        user.setTempPassword(encodedTempPassword, TEMP_PASSWORD_EXPIRATION_MINUTES);

        /// 이메일 발송
        emailService.sendTempPasswordEmail(email, tempPassword, TEMP_PASSWORD_EXPIRATION_MINUTES);
    }

    /// 비밀번호 재설정
    @Transactional
    public void resetPassword(String loginId, String newPassword){
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 회원을 찾을 수 없습니다."));

        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encodedNewPassword);
    }

    /// 영문 + 숫자 (8자리) 임시 비밀번호 생성
    private String generateTempPassword(){
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < TEMP_PASSWORD_LENGTH; i++){
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }






}
