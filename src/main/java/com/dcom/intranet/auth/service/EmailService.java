package com.dcom.intranet.auth.service;

import com.dcom.intranet.auth.domain.EmailVerification;
import com.dcom.intranet.global.exception.BadRequestException;
import com.dcom.intranet.auth.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository emailVerificationRepository;

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 5;

    /// 인증코드발송
    @Transactional
    public void sendVerificationCode(String email) {
        /// 6자리 코드 생성
        String code = generateCode();

        /// DB에 저장(임시)
        EmailVerification verification = new EmailVerification(email, code, EXPIRATION_MINUTES);
        emailVerificationRepository.save(verification);

        /// 메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[D.com Intranet] 이메일 인증 코드");
        message.setText("인증 코드 : " + code + "\n\n"
                + EXPIRATION_MINUTES + "분 내로 입력해주세요. ");
        mailSender.send(message);
    }

    /// 인증 코드 확인
    @Transactional
    public void verifyCode(String email, String code){
        /// 가장 최근 요청 찾기
        EmailVerification verification = emailVerificationRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException("인증 요청을 찾을 수 없습니다."));

        /// 시간 만료시에
        if(verification.isExpired()){
            throw new BadRequestException("인증 코드가 만료되었습니다. 다시 요청해주세요.");
        }
        /// 이미 인증된 경우
        if(verification.isVertified()){
            throw new BadRequestException("이미 인증된 이메일입니다.");
        }
        /// 인증코드 비교
        if(!verification.getCode().equals(code)){
            throw new BadRequestException("인증 코드가 올바르지 않습니다.");
        }

        verification.verify();
    }

    /// 이메일 인증완료 여부 확인
    public boolean isEmailVerified(String email){
        return emailVerificationRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .map(v -> v.isVertified() && !v.isExpired())
                .orElse(false);
    }

    /// 인증코드 생성(6자리)
    private String generateCode(){
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /// 임시 비밀번호 메일 발송
    public void sendTempPasswordEmail(String email, String tempPassword, int expirationMinutes){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[D.com Intranet] 임시 비밀번호 안내");
        message.setText("임시 비밀번호: " + tempPassword + "\n\n"
        + expirationMinutes + "분 내로 로그인하여 비밀번호를 변경해주세요.\n"
        + "로그인 후 반드시 비밀번호를 변경해주세요.");

        mailSender.send(message);
    }
}
