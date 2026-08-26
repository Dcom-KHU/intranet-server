package com.dcom.intranet.auth.service;

import com.dcom.intranet.auth.repository.EmailVerificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailServiceTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final EmailVerificationRepository emailVerificationRepository = mock(EmailVerificationRepository.class);
    private final EmailService emailService = new EmailService(mailSender, emailVerificationRepository);

    @Test
    @DisplayName("회원가입 승인 안내 메일 제목을 D.COM 형식으로 발송한다")
    void sendsApprovalEmailWithDcomSubject() {
        emailService.sendApprovalEmail("member@khu.ac.kr", "홍길동");

        var messageCaptor = forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();
        assertThat(message.getTo()).containsExactly("member@khu.ac.kr");
        assertThat(message.getSubject()).isEqualTo("[D.COM] 인트라넷 회원가입 승인 안내");
        assertThat(message.getText()).contains("홍길동님, 회원가입이 승인되었습니다.");
    }
}
