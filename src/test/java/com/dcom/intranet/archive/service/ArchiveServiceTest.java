package com.dcom.intranet.archive.service;

import com.dcom.intranet.archive.domain.*;
import com.dcom.intranet.archive.dto.response.*;
import com.dcom.intranet.archive.repository.ArchiveRepository;
import com.dcom.intranet.user.User;
import com.dcom.intranet.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ArchiveServiceTest {

    @Autowired
    ArchiveService archiveService;

    @Autowired
    ArchiveRepository archiveRepository;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        archiveRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 최근수정일순으로_족보_아카이브_목록을_조회한다() throws InterruptedException {
        // given
        Archive oldArchive = new Archive("자료구조", "김교수");
        archiveRepository.save(oldArchive);

        Thread.sleep(10);

        Archive newArchive = new Archive("운영체제", "이교수");
        archiveRepository.save(newArchive);

        // when
        ArchivePageResponse<ArchiveListResponse> response =
                archiveService.getArchives(0, 10);

        // then
        assertThat(response.getContent()).hasSize(2);

        assertThat(response.getContent().get(0).getSubjectName())
                .isEqualTo("운영체제");

        assertThat(response.getContent().get(1).getSubjectName())
                .isEqualTo("자료구조");
    }

    @Test
    void 과목명으로_아카이브를_검색한다() {
        // given
        Archive archive1 = new Archive("자료구조", "A교수");
        Archive archive2 = new Archive("자료구조", "B교수");
        Archive archive3 = new Archive("운영체제", "C교수");

        archiveRepository.save(archive1);
        archiveRepository.save(archive2);
        archiveRepository.save(archive3);

        // when
        ArchivePageResponse<ArchiveProfessorGroupResponse> response =
                archiveService.searchBySubjectName("자료구조", 0, 10);

        // then
        assertThat(response.getContent()).hasSize(2);

        assertThat(response.getContent())
                .extracting("subjectName")
                .containsOnly("자료구조");

        assertThat(response.getContent())
                .extracting("professorName")
                .containsExactly("A교수", "B교수");
    }

    @Test
    void 교수명으로_아카이브를_검색한다() {
        // given
        Archive archive1 = new Archive("A과목", "김교수");
        Archive archive2 = new Archive("B과목", "김교수");
        Archive archive3 = new Archive("C과목", "이교수");

        archiveRepository.save(archive1);
        archiveRepository.save(archive2);
        archiveRepository.save(archive3);

        // when
        ArchivePageResponse<ArchiveSubjectGroupResponse> response =
                archiveService.searchByProfessorName("김교수", 0, 10);

        // then
        assertThat(response.getContent()).hasSize(2);

        assertThat(response.getContent())
                .extracting("professorName")
                .containsOnly("김교수");

        assertThat(response.getContent())
                .extracting("subjectName")
                .containsExactly("A과목", "B과목");
    }

    @Test
    void 아카이브_상세_조회를_한다() throws InterruptedException {
        // given
        User user = userRepository.save(
                new User("23", "test@test.com", "하성준", "USER")
        );

        Archive archive = new Archive("오픈소스SW개발방법및도구", "이성원");

        ArchiveRecord oldRecord = new ArchiveRecord(
                user,
                2023,
                Semester.FIRST,
                ExamType.FINAL,
                "2023년 기말 족보입니다."
        );

        ArchiveFile oldFile = new ArchiveFile(
                "23-1 오픈소스 기말.pdf",
                "stored-old.pdf",
                "archive/old.pdf",
                "https://example.com/old.pdf",
                1000L,
                "application/pdf"
        );

        oldRecord.addFile(oldFile);
        archive.addRecord(oldRecord);

        archiveRepository.saveAndFlush(archive);

        Thread.sleep(10);

        ArchiveRecord newRecord = new ArchiveRecord(
                user,
                2024,
                Semester.FIRST,
                ExamType.MIDTERM,
                "2024년 중간 족보입니다."
        );

        ArchiveFile newFile = new ArchiveFile(
                "24-1 오픈소스 중간.pdf",
                "stored-new.pdf",
                "archive/new.pdf",
                "https://example.com/new.pdf",
                2000L,
                "application/pdf"
        );

        newRecord.addFile(newFile);
        archive.addRecord(newRecord);

        Archive savedArchive = archiveRepository.saveAndFlush(archive);

        // when
        ArchiveDetailResponse response =
                archiveService.getArchiveDetail(savedArchive.getId());

        // then
        assertThat(response.getArchiveId()).isEqualTo(savedArchive.getId());
        assertThat(response.getSubjectName()).isEqualTo("오픈소스SW개발방법및도구");
        assertThat(response.getProfessorName()).isEqualTo("이성원");

        assertThat(response.getRecords()).hasSize(2);

        assertThat(response.getRecords().get(0).getExamYear()).isEqualTo(2024);
        assertThat(response.getRecords().get(0).getSemester()).isEqualTo("FIRST");
        assertThat(response.getRecords().get(0).getExamType()).isEqualTo("MIDTERM");
        assertThat(response.getRecords().get(0).getAuthor().getNickname()).isEqualTo("하성준");
        assertThat(response.getRecords().get(0).getFiles()).hasSize(1);
        assertThat(response.getRecords().get(0).getFiles().get(0).getOriginalFileName())
                .isEqualTo("24-1 오픈소스 중간.pdf");

        assertThat(response.getRecords().get(1).getExamYear()).isEqualTo(2023);
    }

    @Test
    void 존재하지_않는_아카이브를_조회하면_예외가_발생한다() {
        assertThatThrownBy(() -> archiveService.getArchiveDetail(999L))
                .isInstanceOf(ResponseStatusException.class);
    }
}