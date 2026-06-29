package com.dcom.intranet.archive.controller;

import com.dcom.intranet.archive.domain.ExamType;
import com.dcom.intranet.archive.domain.Semester;
import com.dcom.intranet.archive.dto.request.ArchiveCreateRequest;
import com.dcom.intranet.archive.dto.request.ArchiveRecordCreateRequest;
import com.dcom.intranet.archive.repository.ArchiveRepository;
import com.dcom.intranet.user.User;
import com.dcom.intranet.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class ArchiveCreateControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ArchiveRepository archiveRepository;

    @Autowired
    UserRepository userRepository;

    User user;

    @BeforeEach
    void setUp() {
        archiveRepository.deleteAll();
        userRepository.deleteAll();

        FileSystemUtils.deleteRecursively(Path.of("./build/test-uploads/archive").toFile());

        user = userRepository.save(
                new User("23", "test@test.com", "하성준", "USER")
        );
    }

    @Test
    void 메인화면에서_새_아카이브와_족보를_등록하고_목록과_상세를_확인한다() throws Exception {
        // given
        ArchiveRecordCreateRequest recordRequest = new ArchiveRecordCreateRequest();
        recordRequest.setExamYear(2024);
        recordRequest.setSemester(Semester.FIRST);
        recordRequest.setExamType(ExamType.MIDTERM);
        recordRequest.setContent("2024년 1학기 중간고사 족보입니다.");
        recordRequest.setFileIndexes(List.of(0, 1));

        ArchiveCreateRequest request = new ArchiveCreateRequest();
        request.setSubjectName("오픈소스SW개발방법및도구");
        request.setProfessorName("이성원");
        request.setRecords(List.of(recordRequest));

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "24-1 오스 중간 기출.pdf",
                "application/pdf",
                "test pdf content 1".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "24-1 오스 중간 예상문제.pdf",
                "application/pdf",
                "test pdf content 2".getBytes()
        );

        // when: 등록 API 호출
        MvcResult createResult = mockMvc.perform(multipart("/api/archives")
                        .file(requestPart)
                        .file(file1)
                        .file(file2)
                        .param("userId", String.valueOf(user.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.archiveId").exists())
                .andExpect(jsonPath("$.recordIds", hasSize(1)))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        System.out.println("\n===== 족보 등록 응답 =====");
        System.out.println(prettyJson(createResponse));

        JsonNode createJson = objectMapper.readTree(createResponse);
        Long archiveId = createJson.get("archiveId").asLong();

        // then: 목록 조회 결과 확인
        MvcResult listResult = mockMvc.perform(get("/api/archives")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].archiveId").value(archiveId))
                .andExpect(jsonPath("$.content[0].subjectName").value("오픈소스SW개발방법및도구"))
                .andExpect(jsonPath("$.content[0].professorName").value("이성원"))
                .andExpect(jsonPath("$.content[0].recordCount").value(1))
                .andReturn();

        System.out.println("\n===== 족보 목록 조회 응답 =====");
        System.out.println(prettyJson(listResult.getResponse().getContentAsString()));

        // then: 상세 조회 결과 확인
        MvcResult detailResult = mockMvc.perform(get("/api/archives/{archiveId}", archiveId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archiveId").value(archiveId))
                .andExpect(jsonPath("$.subjectName").value("오픈소스SW개발방법및도구"))
                .andExpect(jsonPath("$.professorName").value("이성원"))
                .andExpect(jsonPath("$.records", hasSize(1)))
                .andExpect(jsonPath("$.records[0].examYear").value(2024))
                .andExpect(jsonPath("$.records[0].semester").value("FIRST"))
                .andExpect(jsonPath("$.records[0].examType").value("MIDTERM"))
                .andExpect(jsonPath("$.records[0].content").value("2024년 1학기 중간고사 족보입니다."))
                .andExpect(jsonPath("$.records[0].author.nickname").value("하성준"))
                .andExpect(jsonPath("$.records[0].files", hasSize(2)))
                .andExpect(jsonPath("$.records[0].files[0].originalFileName").value("24-1 오스 중간 기출.pdf"))
                .andExpect(jsonPath("$.records[0].files[1].originalFileName").value("24-1 오스 중간 예상문제.pdf"))
                .andReturn();

        System.out.println("\n===== 족보 상세 조회 응답 =====");
        System.out.println(prettyJson(detailResult.getResponse().getContentAsString()));

        assertThat(archiveRepository.count()).isEqualTo(1);
    }

    @Test
    void 상세페이지에서_기존_아카이브에_족보를_추가한다() throws Exception {
        // given: 먼저 메인 화면 방식으로 Archive 하나 생성
        ArchiveRecordCreateRequest firstRecord = new ArchiveRecordCreateRequest();
        firstRecord.setExamYear(2023);
        firstRecord.setSemester(Semester.FIRST);
        firstRecord.setExamType(ExamType.FINAL);
        firstRecord.setContent("기존 족보입니다.");

        ArchiveCreateRequest firstRequest = new ArchiveCreateRequest();
        firstRequest.setSubjectName("자료구조");
        firstRequest.setProfessorName("박제만");
        firstRequest.setRecords(List.of(firstRecord));

        MockMultipartFile firstRequestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(firstRequest)
        );

        MvcResult firstCreateResult = mockMvc.perform(multipart("/api/archives")
                        .file(firstRequestPart)
                        .param("userId", String.valueOf(user.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn();

        Long archiveId = objectMapper.readTree(firstCreateResult.getResponse().getContentAsString())
                .get("archiveId")
                .asLong();

        // given: 상세 페이지에서 archiveId만 가지고 새 record 추가
        ArchiveRecordCreateRequest secondRecord = new ArchiveRecordCreateRequest();
        secondRecord.setExamYear(2024);
        secondRecord.setSemester(Semester.SECOND);
        secondRecord.setExamType(ExamType.QUIZ);
        secondRecord.setContent("상세 페이지에서 추가한 퀴즈 족보입니다.");

        ArchiveCreateRequest secondRequest = new ArchiveCreateRequest();
        secondRequest.setArchiveId(archiveId);
        secondRequest.setRecords(List.of(secondRecord));

        MockMultipartFile secondRequestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(secondRequest)
        );

        // when
        MvcResult secondCreateResult = mockMvc.perform(multipart("/api/archives")
                        .file(secondRequestPart)
                        .param("userId", String.valueOf(user.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.archiveId").value(archiveId))
                .andExpect(jsonPath("$.recordIds", hasSize(1)))
                .andReturn();

        System.out.println("\n===== 기존 아카이브에 족보 추가 응답 =====");
        System.out.println(prettyJson(secondCreateResult.getResponse().getContentAsString()));

        // then: 상세 조회에서 record 2개 확인
        MvcResult detailResult = mockMvc.perform(get("/api/archives/{archiveId}", archiveId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archiveId").value(archiveId))
                .andExpect(jsonPath("$.subjectName").value("자료구조"))
                .andExpect(jsonPath("$.professorName").value("박제만"))
                .andExpect(jsonPath("$.records", hasSize(2)))
                .andReturn();

        System.out.println("\n===== 기존 아카이브 상세 조회 응답 =====");
        System.out.println(prettyJson(detailResult.getResponse().getContentAsString()));

        assertThat(archiveRepository.count()).isEqualTo(1);
    }

    @Test
    void 여러_족보를_한번에_등록하고_상세에서_목록을_확인한다() throws Exception {
        // given
        ArchiveRecordCreateRequest record1 = new ArchiveRecordCreateRequest();
        record1.setExamYear(2024);
        record1.setSemester(Semester.FIRST);
        record1.setExamType(ExamType.MIDTERM);
        record1.setContent("2024 중간 족보");
        record1.setFileIndexes(List.of(0));

        ArchiveRecordCreateRequest record2 = new ArchiveRecordCreateRequest();
        record2.setExamYear(2024);
        record2.setSemester(Semester.FIRST);
        record2.setExamType(ExamType.ASSIGNMENT);
        record2.setContent("2024 과제 자료");
        record2.setFileIndexes(List.of(1));

        ArchiveCreateRequest request = new ArchiveCreateRequest();
        request.setSubjectName("데이터베이스");
        request.setProfessorName("김태언");
        request.setRecords(List.of(record1, record2));

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );

        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "db-midterm.pdf",
                "application/pdf",
                "midterm file".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "db-assignment.pdf",
                "application/pdf",
                "assignment file".getBytes()
        );

        // when
        MvcResult createResult = mockMvc.perform(multipart("/api/archives")
                        .file(requestPart)
                        .file(file1)
                        .file(file2)
                        .param("userId", String.valueOf(user.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.archiveId").exists())
                .andExpect(jsonPath("$.recordIds", hasSize(2)))
                .andReturn();

        System.out.println("\n===== 여러 족보 등록 응답 =====");
        System.out.println(prettyJson(createResult.getResponse().getContentAsString()));

        Long archiveId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("archiveId")
                .asLong();

        // then
        MvcResult detailResult = mockMvc.perform(get("/api/archives/{archiveId}", archiveId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(2)))
                .andReturn();

        System.out.println("\n===== 여러 족보 등록 후 상세 조회 응답 =====");
        System.out.println(prettyJson(detailResult.getResponse().getContentAsString()));
    }

    private String prettyJson(String json) throws Exception {
        Object jsonObject = objectMapper.readValue(json, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
    }
}