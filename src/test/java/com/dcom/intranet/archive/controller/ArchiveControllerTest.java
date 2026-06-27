package com.dcom.intranet.archive.controller;

import com.dcom.intranet.archive.domain.*;
import com.dcom.intranet.archive.repository.ArchiveRepository;
import com.dcom.intranet.user.User;
import com.dcom.intranet.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Security 필터 때문에 401 뜨면 addFilters = false로 임시 우회
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ArchiveControllerTest {

    @Autowired
    MockMvc mockMvc;

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
    void 최근수정일순으로_족보_아카이브_목록을_조회한다() throws Exception {
        // given
        Archive archive1 = new Archive("자료구조", "김교수");
        archiveRepository.save(archive1);

        Thread.sleep(10);

        Archive archive2 = new Archive("운영체제", "이교수");
        archiveRepository.save(archive2);

        // when & then
        mockMvc.perform(get("/api/archives")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].subjectName").value("운영체제"))
                .andExpect(jsonPath("$.content[0].professorName").value("이교수"))
                .andExpect(jsonPath("$.content[1].subjectName").value("자료구조"))
                .andExpect(jsonPath("$.content[1].professorName").value("김교수"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void 족보_아카이브가_없으면_빈_목록을_반환한다() throws Exception {
        mockMvc.perform(get("/api/archives")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void 과목명으로_아카이브를_검색한다() throws Exception {
        // given
        Archive archive1 = new Archive("자료구조", "A교수");
        Archive archive2 = new Archive("자료구조", "B교수");
        Archive archive3 = new Archive("운영체제", "C교수");

        archiveRepository.save(archive1);
        archiveRepository.save(archive2);
        archiveRepository.save(archive3);

        // when & then
        mockMvc.perform(get("/api/archives/search")
                        .param("subjectName", "자료구조")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].subjectName").value("자료구조"))
                .andExpect(jsonPath("$.content[0].professorName").value("A교수"))
                .andExpect(jsonPath("$.content[1].subjectName").value("자료구조"))
                .andExpect(jsonPath("$.content[1].professorName").value("B교수"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void 교수명으로_아카이브를_검색한다() throws Exception {
        // given
        Archive archive1 = new Archive("A과목", "김교수");
        Archive archive2 = new Archive("B과목", "김교수");
        Archive archive3 = new Archive("C과목", "이교수");

        archiveRepository.save(archive1);
        archiveRepository.save(archive2);
        archiveRepository.save(archive3);

        // when & then
        mockMvc.perform(get("/api/archives/search")
                        .param("professorName", "김교수")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].subjectName").value("A과목"))
                .andExpect(jsonPath("$.content[0].professorName").value("김교수"))
                .andExpect(jsonPath("$.content[1].subjectName").value("B과목"))
                .andExpect(jsonPath("$.content[1].professorName").value("김교수"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void 검색어가_없으면_400_에러가_발생한다() throws Exception {
        mockMvc.perform(get("/api/archives/search")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 아카이브_상세_조회를_한다() throws Exception {
        // given
        User user = userRepository.save(
                new User("23", "test@test.com", "하성준", "USER")
        );

        Archive archive = new Archive("오픈소스SW개발방법및도구", "이성원");

        ArchiveRecord record = new ArchiveRecord(
                user,
                2024,
                Semester.FIRST,
                ExamType.MIDTERM,
                "2024년 중간 족보입니다."
        );

        ArchiveFile file = new ArchiveFile(
                "24-1 오픈소스 중간.pdf",
                "stored-new.pdf",
                "archive/new.pdf",
                "https://example.com/new.pdf",
                2000L,
                "application/pdf"
        );

        record.addFile(file);
        archive.addRecord(record);

        Archive savedArchive = archiveRepository.saveAndFlush(archive);

        // when & then
        mockMvc.perform(get("/api/archives/{archiveId}", savedArchive.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archiveId").value(savedArchive.getId()))
                .andExpect(jsonPath("$.subjectName").value("오픈소스SW개발방법및도구"))
                .andExpect(jsonPath("$.professorName").value("이성원"))
                .andExpect(jsonPath("$.records", hasSize(1)))
                .andExpect(jsonPath("$.records[0].examYear").value(2024))
                .andExpect(jsonPath("$.records[0].semester").value("FIRST"))
                .andExpect(jsonPath("$.records[0].examType").value("MIDTERM"))
                .andExpect(jsonPath("$.records[0].content").value("2024년 중간 족보입니다."))
                .andExpect(jsonPath("$.records[0].author.nickname").value("하성준"))
                .andExpect(jsonPath("$.records[0].files", hasSize(1)))
                .andExpect(jsonPath("$.records[0].files[0].originalFileName")
                        .value("24-1 오픈소스 중간.pdf"))
                .andExpect(jsonPath("$.records[0].files[0].fileUrl")
                        .value("https://example.com/new.pdf"));
    }

    @Test
    void 존재하지_않는_아카이브를_조회하면_404가_발생한다() throws Exception {
        mockMvc.perform(get("/api/archives/{archiveId}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}