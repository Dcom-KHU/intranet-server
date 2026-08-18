package com.dcom.intranet.archive.dto.response;

import com.dcom.intranet.archive.domain.ArchiveRecord;
import com.dcom.intranet.auth.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArchiveRecordResponseTest {

    @Test
    @DisplayName("레거시 작성자 정보가 있으면 관리자 계정 대신 레거시 작성자 이름을 내려주고 학번은 숨긴다")
    void usesLegacyAuthorWhenPresent() {
        ArchiveRecord record = archiveRecord();
        record.applyLegacyAuthor("legacy-login-id", "홍길동", false);

        ArchiveRecordResponse response = new ArchiveRecordResponse(record);

        assertThat(response.getAuthor().studentNumber()).isNull();
        assertThat(response.getAuthor().name()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("레거시 익명 글이면 실명과 학번을 노출하지 않는다")
    void hidesLegacyAnonymousAuthor() {
        ArchiveRecord record = archiveRecord();
        record.applyLegacyAuthor(null, null, true);

        ArchiveRecordResponse response = new ArchiveRecordResponse(record);

        assertThat(response.getAuthor().studentNumber()).isNull();
        assertThat(response.getAuthor().name()).isEqualTo("익명");
    }

    @Test
    @DisplayName("레거시 작성자 정보가 없으면 기존 author_id 사용자를 내려준다")
    void fallsBackToRecordAuthor() {
        ArchiveRecord record = archiveRecord();

        ArchiveRecordResponse response = new ArchiveRecordResponse(record);

        assertThat(response.getAuthor().studentNumber()).isEqualTo("20239999");
        assertThat(response.getAuthor().name()).isEqualTo("관리자");
    }

    private ArchiveRecord archiveRecord() {
        User admin = new User(
                "admin",
                "password",
                "관리자",
                "20239999",
                "admin@khu.ac.kr",
                "01012345678"
        );
        return new ArchiveRecord(admin, null, null, null, "본문");
    }
}
