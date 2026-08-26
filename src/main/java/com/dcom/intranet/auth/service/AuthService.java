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
    private final UserAccountLifecycleService userAccountLifecycleService;


    /// 회원가입
    @Transactional
    public SignupResponse signup(SignupRequest request){
        /// 아이디 중복 체크. 논리 삭제된 회원은 같은 아이디로 재가입할 수 있도록 기존 row를 재사용한다.
        User rejoiningUser = userRepository.findByLoginId(request.getLoginId())
                .filter(user -> user.getStatus() == UserStatus.WITHDRAWN)
                .orElse(null);

        if (userRepository.existsByLoginIdAndStatusNot(request.getLoginId(), UserStatus.WITHDRAWN)){
            throw new ConflictException("이미 사용 중인 아이디입니다.");
        }
        if (isStudentIdDuplicated(request.getStudentId(), rejoiningUser)){
            throw new ConflictException("이미 가입된 학번입니다.");
        }
        if (isEmailDuplicated(request.getEmail(), rejoiningUser)){
            throw new ConflictException("이미 사용 중인 이메일입니다.");
        }

        /// 이메일 인증안됐으면 예외처리
        if(!emailService.isEmailVerified(request.getEmail())){
            throw new BadRequestException("이메일 인증이 완료되지 않았습니다.");
        }

        /// 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        if (rejoiningUser != null && userAccountLifecycleService.hasRetainedActivity(rejoiningUser)) {
            rejoiningUser.reactivateForSignup(
                    encodedPassword,
                    request.getName(),
                    request.getStudentId(),
                    request.getEmail(),
                    request.getPhoneNumber()
            );
            return SignupResponse.from(userRepository.save(rejoiningUser));
        }

        if (rejoiningUser != null) {
            userAccountLifecycleService.cleanupAccountAttachments(rejoiningUser);
            userRepository.delete(rejoiningUser);
            userRepository.flush();
        }

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
        boolean exists = userRepository.existsByLoginIdAndStatusNot(loginId, UserStatus.WITHDRAWN);
        return CheckLoginIdResponse.of(!exists);
    }

    /// 로그인
    @Transactional
    public LoginResponse login(LoginRequest request) {

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
                new RefreshToken(refreshToken, user.getLoginId(), jwtTokenProvider.getRefreshTokenValidity())
        );

        long expiresIn = jwtTokenProvider.getAccessTokenValidity() / 1000;

        return LoginResponse.of(user, accessToken, refreshToken, expiresIn, usedTempPassword);

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
        if(savedToken.isExpired() || !jwtTokenProvider.validateToken(request.getRefreshToken())){
            refreshTokenRepository.delete(savedToken);
            throw new UnauthorizedException("로그인이 만료되었습니다. 다시 로그인해주세요.");
        }

        /// 토큰에서 loginId 추출 후 DB에서 현재 상태/role 재조회 (탈퇴, 권한 이양 등의 변경 사항 반영)
        String loginId = jwtTokenProvider.getLoginId(request.getRefreshToken());

        if (!savedToken.getLoginId().equals(loginId)) {
            refreshTokenRepository.delete(savedToken);
            throw new UnauthorizedException("유효하지 않은 RefreshToken입니다.");
        }

        User user = userRepository.findByLoginId(loginId)
                .orElse(null);

        if (user == null) {
            refreshTokenRepository.delete(savedToken);
            throw new UnauthorizedException("유효하지 않은 RefreshToken입니다.");
        }

        /// 탈퇴 등으로 더 이상 APPROVED 상태가 아니면 재발급 거부 + 보유한 Refresh Token 전체 삭제
        if (user.getStatus() != UserStatus.APPROVED) {
            refreshTokenRepository.deleteByLoginId(loginId);
            throw new UnauthorizedException("더 이상 유효하지 않은 계정입니다. 다시 로그인해주세요.");
        }

        String role = user.getRole().name();

        /// 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(loginId, role);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(loginId, role);


        /// 기존 리프레시 토큰 삭제 + 새거 저장(Rotation)
        refreshTokenRepository.delete(savedToken);
        refreshTokenRepository.save(new RefreshToken(newRefreshToken, loginId, jwtTokenProvider.getRefreshTokenValidity()));

        long expiresIn = jwtTokenProvider.getAccessTokenValidity() / 1000;

        return RefreshResponse.of(newAccessToken, newRefreshToken, expiresIn);

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

        /// 비밀번호가 재설정됐으므로 기존 세션(Refresh Token)은 모두 무효화
        refreshTokenRepository.deleteByLoginId(loginId);
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

    private boolean isStudentIdDuplicated(String studentId, User rejoiningUser) {
        if (rejoiningUser == null) {
            return userRepository.existsByStudentId(studentId);
        }
        return userRepository.existsByStudentIdAndIdNot(studentId, rejoiningUser.getId());
    }

    private boolean isEmailDuplicated(String email, User rejoiningUser) {
        if (rejoiningUser == null) {
            return userRepository.existsByEmail(email);
        }
        return userRepository.existsByEmailAndIdNot(email, rejoiningUser.getId());
    }






}
