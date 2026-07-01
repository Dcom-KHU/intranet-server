package com.dcom.intranet.archive.controller;

import com.dcom.intranet.archive.domain.*;
import com.dcom.intranet.archive.dto.request.ArchiveCreateRequest;
import com.dcom.intranet.archive.dto.request.ArchiveRecordCreateRequest;
import com.dcom.intranet.archive.repository.ArchiveFileRepository;
import com.dcom.intranet.archive.repository.ArchiveRecordRepository;
import com.dcom.intranet.archive.repository.ArchiveRepository;
import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "file.upload-dir=./build/test-uploads/archive"
})
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ArchiveControllerTest {

    private static final Path TEST_UPLOAD_DIR = Path.of("./build/test-uploads/archive");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ArchiveRepository archiveRepository;

    @Autowired
    ArchiveRecordRepository archiveRecordRepository;

    @Autowired
    ArchiveFileRepository archiveFileRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ObjectMapper objectMapper;

    User user;

    @BeforeEach
    void setUp() {
        archiveFileRepository.deleteAllInBatch();
        archiveRecordRepository.deleteAllInBatch();
        archiveRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        FileSystemUtils.deleteRecursively(TEST_UPLOAD_DIR.toFile());

        long now = System.nanoTime();

        user = userRepository.save(
                new User("TEST-" + now, "test" + now + "@test.com", "하성준", "USER")
        );
    }

    @AfterEach
    void tearDown() {
        FileSystemUtils.deleteRecursively(TEST_UPLOAD_DIR.toFile());
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
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].subjectName").value("운영체제"))
                .andExpect(jsonPath("$.data.content[0].professorName").value("이교수"))
                .andExpect(jsonPath("$.data.content[1].subjectName").value("자료구조"))
                .andExpect(jsonPath("$.data.content[1].professorName").value("김교수"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void 족보_아카이브가_없으면_빈_목록을_반환한다() throws Exception {
        mockMvc.perform(get("/api/archives")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(0));
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
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].subjectName").value("자료구조"))
                .andExpect(jsonPath("$.data.content[0].professorName").value("A교수"))
                .andExpect(jsonPath("$.data.content[1].subjectName").value("자료구조"))
                .andExpect(jsonPath("$.data.content[1].professorName").value("B교수"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(2));
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
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].subjectName").value("A과목"))
                .andExpect(jsonPath("$.data.content[0].professorName").value("김교수"))
                .andExpect(jsonPath("$.data.content[1].subjectName").value("B과목"))
                .andExpect(jsonPath("$.data.content[1].professorName").value("김교수"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    void 검색어가_없으면_400_에러가_발생한다() throws Exception {
        mockMvc.perform(get("/api/archives/search")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("subjectName 또는 professorName 중 하나는 필요합니다."));
    }

    @Test
    void 아카이브_상세_조회를_한다() throws Exception {
        // given
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
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.archiveId").value(savedArchive.getId()))
                .andExpect(jsonPath("$.data.subjectName").value("오픈소스SW개발방법및도구"))
                .andExpect(jsonPath("$.data.professorName").value("이성원"))
                .andExpect(jsonPath("$.data.records", hasSize(1)))
                .andExpect(jsonPath("$.data.records[0].examYear").value(2024))
                .andExpect(jsonPath("$.data.records[0].semester").value("FIRST"))
                .andExpect(jsonPath("$.data.records[0].examType").value("MIDTERM"))
                .andExpect(jsonPath("$.data.records[0].content").value("2024년 중간 족보입니다."))
                .andExpect(jsonPath("$.data.records[0].author.nickname").value("하성준"))
                .andExpect(jsonPath("$.data.records[0].files", hasSize(1)))
                .andExpect(jsonPath("$.data.records[0].files[0].originalFileName")
                        .value("24-1 오픈소스 중간.pdf"))
                .andExpect(jsonPath("$.data.records[0].files[0].fileUrl")
                        .value("https://example.com/new.pdf"));
    }

    @Test
    void 존재하지_않는_아카이브를_조회하면_404가_발생한다() throws Exception {
        mockMvc.perform(get("/api/archives/{archiveId}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void 메인화면_등록에서_과목명과_교수명이_없으면_400_에러를_반환한다() throws Exception {
        // given
        ArchiveRecordCreateRequest recordRequest = new ArchiveRecordCreateRequest();
        recordRequest.setExamYear(2024);
        recordRequest.setSemester(Semester.FIRST);
        recordRequest.setExamType(ExamType.MIDTERM);
        recordRequest.setContent("본문 내용");

        ArchiveCreateRequest request = new ArchiveCreateRequest();
        request.setRecords(List.of(recordRequest));

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        // when & then
        MvcResult result = mockMvc.perform(multipart("/api/archives")
                        .file(requestPart)
                        .param("userId", String.valueOf(user.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("archiveId가 없으면 subjectName과 professorName은 필수입니다."))
                .andReturn();

        System.out.println("\n===== 필수 입력값 누락 에러 응답 =====");
        System.out.println(prettyJson(result.getResponse().getContentAsString()));
    }

    @Test
    void 업로드된_파일이_서버_로컬_저장소에_저장된다() throws Exception {
        // given
        ArchiveRecordCreateRequest recordRequest = new ArchiveRecordCreateRequest();
        recordRequest.setExamYear(2024);
        recordRequest.setSemester(Semester.FIRST);
        recordRequest.setExamType(ExamType.MIDTERM);
        recordRequest.setContent("파일 저장 테스트");
        recordRequest.setFileIndexes(List.of(0));

        ArchiveCreateRequest request = new ArchiveCreateRequest();
        request.setSubjectName("운영체제");
        request.setProfessorName("김교수");
        request.setRecords(List.of(recordRequest));

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "os-midterm.pdf",
                "application/pdf",
                "test file content".getBytes()
        );

        // when
        MvcResult createResult = mockMvc.perform(multipart("/api/archives")
                        .file(requestPart)
                        .file(file)
                        .param("userId", String.valueOf(user.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.archiveId").exists())
                .andExpect(jsonPath("$.data.recordIds", hasSize(1)))
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andReturn();

        System.out.println("\n===== 파일 업로드 등록 응답 =====");
        System.out.println(prettyJson(createResult.getResponse().getContentAsString()));

        // then
        Path uploadRoot = Path.of("./build/test-uploads/archive");

        List<Path> savedFiles;
        try (var paths = java.nio.file.Files.walk(uploadRoot)) {
            savedFiles = paths
                    .filter(java.nio.file.Files::isRegularFile)
                    .toList();
        }

        System.out.println("\n===== 로컬 저장소에 저장된 파일 목록 =====");
        savedFiles.forEach(path -> System.out.println(path.toAbsolutePath()));

        assertThat(savedFiles).hasSize(1);
        assertThat(savedFiles.get(0).getFileName().toString()).endsWith(".pdf");
    }

    private String prettyJson(String json) throws Exception {
        Object jsonObject = objectMapper.readValue(json, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(jsonObject);
    }
}