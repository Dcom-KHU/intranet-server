package com.dcom.intranet.info.controller;

import com.dcom.intranet.info.domain.InfoPost;
import com.dcom.intranet.info.domain.InfoPostFile;
import com.dcom.intranet.info.dto.request.InfoPostCreateRequest;
import com.dcom.intranet.info.dto.request.InfoPostUpdateRequest;
import com.dcom.intranet.info.repository.InfoPostFileRepository;
import com.dcom.intranet.info.repository.InfoPostRepository;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.FileSystemUtils;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "file.upload-dir=./build/test-uploads/info"
})
class InfoPostControllerTest {

    private static final Path TEST_UPLOAD_DIR = Path.of("./build/test-uploads/info");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    InfoPostRepository infoPostRepository;

    @Autowired
    InfoPostFileRepository infoPostFileRepository;

    @Autowired
    UserRepository userRepository;

    User owner;
    User otherUser;
    User admin;

    @BeforeEach
    void setUp() throws Exception {
        infoPostFileRepository.deleteAllInBatch();
        infoPostRepository.deleteAllInBatch();

        FileSystemUtils.deleteRecursively(TEST_UPLOAD_DIR.toFile());

        String suffix = String.valueOf(System.nanoTime() % 1_000_000_000L);

        owner = userRepository.save(
                new User(
                        "10" + suffix,
                        "owner" + suffix + "@test.com",
                        "작성자",
                        "USER"
                )
        );

        otherUser = userRepository.save(
                new User(
                        "20" + suffix,
                        "other" + suffix + "@test.com",
                        "다른유저",
                        "USER"
                )
        );

        admin = userRepository.save(
                new User(
                        "30" + suffix,
                        "admin" + suffix + "@test.com",
                        "관리자",
                        "ADMIN"
                )
        );
    }

    @Test
    void 게시글_목록을_조회한다() throws Exception {
        // given
        InfoPost oldPost = new InfoPost(owner, "운영체제 정리", "프로세스와 스레드 정리");
        infoPostRepository.saveAndFlush(oldPost);

        Thread.sleep(10);

        InfoPost newPost = new InfoPost(owner, "시간 복잡도 Big-O 핵심 정리", "알고리즘 성능 분석");
        infoPostRepository.saveAndFlush(newPost);

        // when & then
        mockMvc.perform(get("/api/info-posts")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data.postList", hasSize(2)))
                .andExpect(jsonPath("$.data.postList[0].title").value("시간 복잡도 Big-O 핵심 정리"))
                .andExpect(jsonPath("$.data.postList[1].title").value("운영체제 정리"))
                .andExpect(jsonPath("$.data.pageInfo.totalElements").value(2));
    }

    @Test
    void 제목_또는_본문_기준으로_게시글을_검색한다() throws Exception {
        // given
        infoPostRepository.saveAndFlush(
                new InfoPost(owner, "TCP 3-way handshake 정리", "네트워크 연결 과정")
        );

        infoPostRepository.saveAndFlush(
                new InfoPost(owner, "DB 인덱스 구조", "B-Tree 중심 정리")
        );

        // when & then
        mockMvc.perform(get("/api/info-posts")
                        .param("keyword", "TCP")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postList", hasSize(1)))
                .andExpect(jsonPath("$.data.postList[0].title").value("TCP 3-way handshake 정리"));
    }

    @Test
    void 게시글_상세_조회를_한다() throws Exception {
        // given
        InfoPost post = new InfoPost(
                owner,
                "시간 복잡도 Big-O 핵심 정리",
                "시간 복잡도는 알고리즘 성능을 평가하는 기준입니다."
        );

        InfoPost savedPost = infoPostRepository.saveAndFlush(post);

        // when & then
        mockMvc.perform(get("/api/info-posts/{postId}", savedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.postId").value(savedPost.getId()))
                .andExpect(jsonPath("$.data.title").value("시간 복잡도 Big-O 핵심 정리"))
                .andExpect(jsonPath("$.data.content").value("시간 복잡도는 알고리즘 성능을 평가하는 기준입니다."))
                .andExpect(jsonPath("$.data.authorId").value(owner.getId()))
                .andExpect(jsonPath("$.data.authorName").value("작성자"))
                .andExpect(jsonPath("$.data.views").value(1));
    }

    @Test
    void 존재하지_않는_게시글을_상세_조회하면_404를_반환한다() throws Exception {
        mockMvc.perform(get("/api/info-posts/{postId}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("정보 공유 게시글을 찾을 수 없습니다."));
    }

    @Test
    void 게시글을_작성한다() throws Exception {
        // given
        InfoPostCreateRequest request = new InfoPostCreateRequest();
        request.setTitle("시간 복잡도 Big-O 핵심 정리");
        request.setContent("시간 복잡도는 알고리즘 성능을 평가하는 기준입니다.");

        MockMultipartFile requestPart = jsonPart(request);

        // when & then
        mockMvc.perform(multipart("/api/info-posts")
                        .file(requestPart)
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("게시글이 작성되었습니다."))
                .andExpect(jsonPath("$.data.title").value("시간 복잡도 Big-O 핵심 정리"))
                .andExpect(jsonPath("$.data.content").value("시간 복잡도는 알고리즘 성능을 평가하는 기준입니다."))
                .andExpect(jsonPath("$.data.authorId").value(owner.getId()))
                .andExpect(jsonPath("$.data.files", hasSize(0)));

        assertThat(infoPostRepository.findAll()).hasSize(1);
    }

    @Test
    void 파일을_첨부해서_게시글을_작성한다() throws Exception {
        // given
        InfoPostCreateRequest request = new InfoPostCreateRequest();
        request.setTitle("파일 첨부 게시글");
        request.setContent("첨부파일이 있는 게시글입니다.");

        MockMultipartFile requestPart = jsonPart(request);

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "big-o-summary.pdf",
                "application/pdf",
                "pdf-content".getBytes(StandardCharsets.UTF_8)
        );

        // when
        MvcResult result = mockMvc.perform(multipart("/api/info-posts")
                        .file(requestPart)
                        .file(file)
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.files", hasSize(1)))
                .andExpect(jsonPath("$.data.files[0].originalFileName").value("big-o-summary.pdf"))
                .andReturn();

        // then
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String fileUrl = json.get("data").get("files").get(0).get("fileUrl").asText();

        assertThat(Files.exists(Path.of(fileUrl))).isTrue();
        assertThat(infoPostFileRepository.findAll()).hasSize(1);
    }

    @Test
    void 작성자가_게시글을_수정한다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "수정 전 제목", "수정 전 본문")
        );

        InfoPostUpdateRequest request = new InfoPostUpdateRequest();
        request.setTitle("수정 후 제목");
        request.setContent("수정 후 본문");
        request.setDeleteFileIds(List.of());

        MockMultipartFile requestPart = jsonPart(request);

        // when & then
        mockMvc.perform(multipart("/api/info-posts/{postId}", post.getId())
                        .file(requestPart)
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(httpRequest -> {
                            httpRequest.setMethod("PUT");
                            return httpRequest;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("게시글이 수정되었습니다."))
                .andExpect(jsonPath("$.data.postId").value(post.getId()))
                .andExpect(jsonPath("$.data.title").value("수정 후 제목"))
                .andExpect(jsonPath("$.data.content").value("수정 후 본문"));
    }

    @Test
    void 작성자가_아닌_사용자는_게시글을_수정할_수_없다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "수정 권한 테스트", "본문")
        );

        InfoPostUpdateRequest request = new InfoPostUpdateRequest();
        request.setTitle("다른 유저가 수정");
        request.setContent("다른 유저가 수정한 본문");
        request.setDeleteFileIds(List.of());

        MockMultipartFile requestPart = jsonPart(request);

        // when & then
        mockMvc.perform(multipart("/api/info-posts/{postId}", post.getId())
                        .file(requestPart)
                        .param("userId", String.valueOf(otherUser.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(httpRequest -> {
                            httpRequest.setMethod("PUT");
                            return httpRequest;
                        }))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("작성자만 수정할 수 있습니다."));
    }

    @Test
    void 기존_파일을_삭제하고_새_파일을_추가한다() throws Exception {
        // given
        InfoPost post = new InfoPost(owner, "파일 수정 테스트", "본문");

        Path oldFilePath = TEST_UPLOAD_DIR.resolve("old-file.pdf");
        Files.createDirectories(TEST_UPLOAD_DIR);
        Files.writeString(oldFilePath, "old-file-content");

        InfoPostFile oldFile = new InfoPostFile(
                "old-file.pdf",
                "stored-old-file.pdf",
                "old-file.pdf",
                oldFilePath.toString(),
                100L,
                "application/pdf"
        );

        post.addFile(oldFile);

        InfoPost savedPost = infoPostRepository.saveAndFlush(post);
        Long oldFileId = savedPost.getFiles().get(0).getId();

        InfoPostUpdateRequest request = new InfoPostUpdateRequest();
        request.setTitle("파일 수정 완료");
        request.setContent("기존 파일 삭제 후 새 파일 추가");
        request.setDeleteFileIds(List.of(oldFileId));

        MockMultipartFile requestPart = jsonPart(request);

        MockMultipartFile newFile = new MockMultipartFile(
                "files",
                "new-file.pdf",
                "application/pdf",
                "new-file-content".getBytes(StandardCharsets.UTF_8)
        );

        // when
        MvcResult result = mockMvc.perform(multipart("/api/info-posts/{postId}", savedPost.getId())
                        .file(requestPart)
                        .file(newFile)
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(httpRequest -> {
                            httpRequest.setMethod("PUT");
                            return httpRequest;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.files", hasSize(1)))
                .andExpect(jsonPath("$.data.files[0].originalFileName").value("new-file.pdf"))
                .andReturn();

        // then
        assertThat(Files.exists(oldFilePath)).isFalse();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        String newFileUrl = json.get("data").get("files").get(0).get("fileUrl").asText();

        assertThat(Files.exists(Path.of(newFileUrl))).isTrue();
    }

    @Test
    void 작성자는_게시글을_삭제할_수_있다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "삭제 테스트", "삭제할 게시글")
        );

        // when & then
        mockMvc.perform(delete("/api/info-posts/{postId}", post.getId())
                        .param("userId", String.valueOf(owner.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("게시글이 삭제되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        assertThat(infoPostRepository.findById(post.getId())).isEmpty();
    }

    @Test
    void 관리자는_게시글을_삭제할_수_있다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "관리자 삭제 테스트", "관리자가 삭제할 게시글")
        );

        // when & then
        mockMvc.perform(delete("/api/info-posts/{postId}", post.getId())
                        .param("userId", String.valueOf(admin.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글이 삭제되었습니다."));

        assertThat(infoPostRepository.findById(post.getId())).isEmpty();
    }

    @Test
    void 작성자도_관리자도_아닌_사용자는_게시글을_삭제할_수_없다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "삭제 권한 테스트", "본문")
        );

        // when & then
        mockMvc.perform(delete("/api/info-posts/{postId}", post.getId())
                        .param("userId", String.valueOf(otherUser.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("작성자 또는 관리자만 삭제할 수 있습니다."));

        assertThat(infoPostRepository.findById(post.getId())).isPresent();
    }

    @Test
    void 게시글_삭제_시_첨부파일도_로컬_저장소에서_삭제된다() throws Exception {
        // given
        InfoPost post = new InfoPost(owner, "파일 삭제 테스트", "첨부파일 삭제 확인");

        Path filePath = TEST_UPLOAD_DIR.resolve("delete-target.pdf");
        Files.createDirectories(TEST_UPLOAD_DIR);
        Files.writeString(filePath, "delete-target-content");

        InfoPostFile file = new InfoPostFile(
                "delete-target.pdf",
                "stored-delete-target.pdf",
                "delete-target.pdf",
                filePath.toString(),
                100L,
                "application/pdf"
        );

        post.addFile(file);

        InfoPost savedPost = infoPostRepository.saveAndFlush(post);

        assertThat(Files.exists(filePath)).isTrue();

        // when
        mockMvc.perform(delete("/api/info-posts/{postId}", savedPost.getId())
                        .param("userId", String.valueOf(owner.getId())))
                .andExpect(status().isOk());

        // then
        assertThat(infoPostRepository.findById(savedPost.getId())).isEmpty();
        assertThat(infoPostFileRepository.findAll()).isEmpty();
        assertThat(Files.exists(filePath)).isFalse();
    }

    private MockMultipartFile jsonPart(Object request) throws Exception {
        return new MockMultipartFile(
                "request",
                "",
                MediaType.TEXT_PLAIN_VALUE,
                objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
        );
    }
}