package com.dcom.intranet.archive.controller;

import com.dcom.intranet.archive.domain.ExamType;
import com.dcom.intranet.archive.domain.Semester;
import com.dcom.intranet.archive.dto.request.ArchiveCreateRequest;
import com.dcom.intranet.archive.dto.request.ArchiveRecordCreateRequest;
import com.dcom.intranet.archive.dto.request.ArchiveUpdateRequest;
import com.dcom.intranet.archive.repository.ArchiveRepository;
import com.dcom.intranet.user.User;
import com.dcom.intranet.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
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

import java.nio.file.Files;
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
class ArchiveUpdateControllerTest {

    private static final Path TEST_UPLOAD_DIR = Path.of("./build/test-uploads/archive");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ArchiveRepository archiveRepository;

    @Autowired
    UserRepository userRepository;

    User owner;
    User otherUser;

    @BeforeEach
    void setUp() {
        archiveRepository.deleteAll();
        userRepository.deleteAll();
        FileSystemUtils.deleteRecursively(TEST_UPLOAD_DIR.toFile());

        owner = userRepository.save(
                new User("20240001", "owner@test.com", "작성자", "USER")
        );

        otherUser = userRepository.save(
                new User("20240002", "other@test.com", "다른유저", "USER")
        );
    }

    @AfterEach
    void tearDown() {
        FileSystemUtils.deleteRecursively(TEST_UPLOAD_DIR.toFile());
    }

    @Test
    void 작성자가_족보_기본정보를_수정한다() throws Exception {
        // given
        CreatedArchive created = createArchiveWithTwoFiles();

        ArchiveUpdateRequest updateRequest = new ArchiveUpdateRequest();
        updateRequest.setExamYear(2025);
        updateRequest.setSemester(Semester.SECOND);
        updateRequest.setExamType(ExamType.FINAL);
        updateRequest.setContent("수정된 기말고사 족보 내용입니다.");
        updateRequest.setDeleteFileIds(List.of());

        MockMultipartFile requestPart = jsonPart(updateRequest);

        // when
        MvcResult updateResult = mockMvc.perform(multipart(
                        "/api/archives/{archiveId}/records/{recordId}",
                        created.archiveId(),
                        created.recordId()
                )
                        .file(requestPart)
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordId").value(created.recordId()))
                .andExpect(jsonPath("$.updatedAt").exists())
                .andReturn();

        System.out.println("\n===== 수정 응답 =====");
        System.out.println(prettyJson(updateResult.getResponse().getContentAsString()));

        // then
        MvcResult detailResult = mockMvc.perform(get("/api/archives/{archiveId}", created.archiveId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].examYear").value(2025))
                .andExpect(jsonPath("$.records[0].semester").value("SECOND"))
                .andExpect(jsonPath("$.records[0].examType").value("FINAL"))
                .andExpect(jsonPath("$.records[0].content").value("수정된 기말고사 족보 내용입니다."))
                .andExpect(jsonPath("$.records[0].files", hasSize(2)))
                .andReturn();

        System.out.println("\n===== 수정 후 상세 조회 =====");
        System.out.println(prettyJson(detailResult.getResponse().getContentAsString()));
    }

    @Test
    void 기존_파일_일부를_삭제하고_새_파일을_추가한다() throws Exception {
        // given
        CreatedArchive created = createArchiveWithTwoFiles();

        MvcResult beforeDetail = mockMvc.perform(get("/api/archives/{archiveId}", created.archiveId()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode beforeJson = objectMapper.readTree(beforeDetail.getResponse().getContentAsString());
        JsonNode files = beforeJson.get("records").get(0).get("files");

        Long deleteFileId = files.get(0).get("fileId").asLong();
        String deletedFileUrl = files.get(0).get("fileUrl").asText();

        ArchiveUpdateRequest updateRequest = new ArchiveUpdateRequest();
        updateRequest.setExamYear(2024);
        updateRequest.setSemester(Semester.FIRST);
        updateRequest.setExamType(ExamType.MIDTERM);
        updateRequest.setContent("파일 일부 삭제 후 새 파일 추가");
        updateRequest.setDeleteFileIds(List.of(deleteFileId));

        MockMultipartFile requestPart = jsonPart(updateRequest);

        MockMultipartFile newFile = new MockMultipartFile(
                "files",
                "C-new-file.pdf",
                "application/pdf",
                "new file content".getBytes()
        );

        // when
        MvcResult updateResult = mockMvc.perform(multipart(
                        "/api/archives/{archiveId}/records/{recordId}",
                        created.archiveId(),
                        created.recordId()
                )
                        .file(requestPart)
                        .file(newFile)
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("\n===== 파일 삭제 + 추가 수정 응답 =====");
        System.out.println(prettyJson(updateResult.getResponse().getContentAsString()));

        // then: 상세 조회에서 파일은 B 유지 + C 추가 = 총 2개
        MvcResult afterDetail = mockMvc.perform(get("/api/archives/{archiveId}", created.archiveId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].files", hasSize(2)))
                .andExpect(jsonPath("$.records[0].files[0].originalFileName").value("B-file.pdf"))
                .andExpect(jsonPath("$.records[0].files[1].originalFileName").value("C-new-file.pdf"))
                .andReturn();

        System.out.println("\n===== 파일 수정 후 상세 조회 =====");
        System.out.println(prettyJson(afterDetail.getResponse().getContentAsString()));

        // 로컬 저장소에서도 삭제 대상 파일이 없어졌는지 확인
        assertThat(Files.exists(Path.of(deletedFileUrl))).isFalse();

        List<Path> savedFiles;
        try (var paths = Files.walk(TEST_UPLOAD_DIR)) {
            savedFiles = paths.filter(Files::isRegularFile).toList();
        }

        System.out.println("\n===== 수정 후 로컬 저장소 파일 목록 =====");
        savedFiles.forEach(path -> System.out.println(path.toAbsolutePath()));

        assertThat(savedFiles).hasSize(2);
    }

    @Test
    void 작성자가_아닌_사용자는_수정할_수_없다() throws Exception {
        // given
        CreatedArchive created = createArchiveWithTwoFiles();

        ArchiveUpdateRequest updateRequest = new ArchiveUpdateRequest();
        updateRequest.setExamYear(2025);
        updateRequest.setSemester(Semester.SECOND);
        updateRequest.setExamType(ExamType.FINAL);
        updateRequest.setContent("다른 유저가 수정 시도");
        updateRequest.setDeleteFileIds(List.of());

        MockMultipartFile requestPart = jsonPart(updateRequest);

        // when & then
        MvcResult result = mockMvc.perform(multipart(
                        "/api/archives/{archiveId}/records/{recordId}",
                        created.archiveId(),
                        created.recordId()
                )
                        .file(requestPart)
                        .param("userId", String.valueOf(otherUser.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isForbidden())
                .andReturn();

        System.out.println("\n===== 권한 없는 수정 에러 응답 =====");
        System.out.println(prettyJson(result.getResponse().getContentAsString()));
    }

    @Test
    void 본문도_없고_파일도_없는_상태가_되면_400_에러를_반환한다() throws Exception {
        // given
        CreatedArchive created = createArchiveWithTwoFiles();

        MvcResult beforeDetail = mockMvc.perform(get("/api/archives/{archiveId}", created.archiveId()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode beforeJson = objectMapper.readTree(beforeDetail.getResponse().getContentAsString());
        JsonNode files = beforeJson.get("records").get(0).get("files");

        Long fileId1 = files.get(0).get("fileId").asLong();
        Long fileId2 = files.get(1).get("fileId").asLong();

        ArchiveUpdateRequest updateRequest = new ArchiveUpdateRequest();
        updateRequest.setExamYear(2024);
        updateRequest.setSemester(Semester.FIRST);
        updateRequest.setExamType(ExamType.MIDTERM);
        updateRequest.setContent("");
        updateRequest.setDeleteFileIds(List.of(fileId1, fileId2));

        MockMultipartFile requestPart = jsonPart(updateRequest);

        // when & then
        MvcResult result = mockMvc.perform(multipart(
                        "/api/archives/{archiveId}/records/{recordId}",
                        created.archiveId(),
                        created.recordId()
                )
                        .file(requestPart)
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andReturn();

        System.out.println("\n===== 빈 족보 수정 에러 응답 =====");
        System.out.println(prettyJson(result.getResponse().getContentAsString()));
    }

    private CreatedArchive createArchiveWithTwoFiles() throws Exception {
        ArchiveRecordCreateRequest recordRequest = new ArchiveRecordCreateRequest();
        recordRequest.setExamYear(2024);
        recordRequest.setSemester(Semester.FIRST);
        recordRequest.setExamType(ExamType.MIDTERM);
        recordRequest.setContent("기존 중간고사 족보입니다.");
        recordRequest.setFileIndexes(List.of(0, 1));

        ArchiveCreateRequest createRequest = new ArchiveCreateRequest();
        createRequest.setSubjectName("자료구조");
        createRequest.setProfessorName("박교수");
        createRequest.setRecords(List.of(recordRequest));

        MockMultipartFile requestPart = new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(createRequest)
        );

        MockMultipartFile fileA = new MockMultipartFile(
                "files",
                "A-file.pdf",
                "application/pdf",
                "A file content".getBytes()
        );

        MockMultipartFile fileB = new MockMultipartFile(
                "files",
                "B-file.pdf",
                "application/pdf",
                "B file content".getBytes()
        );

        MvcResult createResult = mockMvc.perform(multipart("/api/archives")
                        .file(requestPart)
                        .file(fileA)
                        .file(fileB)
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long archiveId = createJson.get("archiveId").asLong();
        Long recordId = createJson.get("recordIds").get(0).asLong();

        return new CreatedArchive(archiveId, recordId);
    }

    private MockMultipartFile jsonPart(ArchiveUpdateRequest request) throws Exception {
        return new MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(request)
        );
    }

    private String prettyJson(String json) throws Exception {
        Object jsonObject = objectMapper.readValue(json, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(jsonObject);
    }

    private record CreatedArchive(Long archiveId, Long recordId) {
    }
}