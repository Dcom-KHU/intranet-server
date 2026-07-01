package com.dcom.intranet.info.controller;

import com.dcom.intranet.info.domain.InfoComment;
import com.dcom.intranet.info.domain.InfoPost;
import com.dcom.intranet.info.dto.request.InfoCommentCreateRequest;
import com.dcom.intranet.info.dto.request.InfoCommentUpdateRequest;
import com.dcom.intranet.info.repository.InfoCommentRepository;
import com.dcom.intranet.info.repository.InfoPostFileRepository;
import com.dcom.intranet.info.repository.InfoPostRepository;
import com.dcom.intranet.user.User;
import com.dcom.intranet.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "file.upload-dir=./build/test-uploads/info"
})
class InfoCommentControllerTest {

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
    InfoCommentRepository infoCommentRepository;

    @Autowired
    UserRepository userRepository;

    User owner;
    User otherUser;
    User admin;

    @BeforeEach
    void setUp() throws Exception {
        infoCommentRepository.deleteAllInBatch();
        infoPostFileRepository.deleteAllInBatch();
        infoPostRepository.deleteAllInBatch();

        FileSystemUtils.deleteRecursively(TEST_UPLOAD_DIR.toFile());

        String suffix = String.valueOf(System.nanoTime() % 1_000_000_000L);

        owner = userRepository.save(
                new User(
                        "10" + suffix,
                        "comment-owner" + suffix + "@test.com",
                        "댓글작성자",
                        "USER"
                )
        );

        otherUser = userRepository.save(
                new User(
                        "20" + suffix,
                        "comment-other" + suffix + "@test.com",
                        "다른유저",
                        "USER"
                )
        );

        admin = userRepository.save(
                new User(
                        "30" + suffix,
                        "comment-admin" + suffix + "@test.com",
                        "관리자",
                        "ADMIN"
                )
        );
    }

    @Test
    void 특정_게시글의_댓글_목록을_조회한다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "정보공유 게시글", "본문입니다.")
        );

        InfoComment comment1 = new InfoComment(post, owner, "첫 번째 댓글입니다.");
        InfoComment comment2 = new InfoComment(post, otherUser, "두 번째 댓글입니다.");

        infoCommentRepository.saveAndFlush(comment1);
        infoCommentRepository.saveAndFlush(comment2);

        // when & then
        mockMvc.perform(get("/api/info-posts/{postId}/comments", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("요청이 성공적으로 처리되었습니다."))
                .andExpect(jsonPath("$.data.comments", hasSize(2)))
                .andExpect(jsonPath("$.data.comments[0].content").value("첫 번째 댓글입니다."))
                .andExpect(jsonPath("$.data.comments[1].content").value("두 번째 댓글입니다."));
    }

    @Test
    void 존재하지_않는_게시글의_댓글_목록을_조회하면_404를_반환한다() throws Exception {
        mockMvc.perform(get("/api/info-posts/{postId}/comments", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("정보 공유 게시글을 찾을 수 없습니다."));
    }

    @Test
    void 댓글을_작성한다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "댓글 작성 테스트 게시글", "본문입니다.")
        );

        InfoCommentCreateRequest request = new InfoCommentCreateRequest();
        request.setContent("좋은 정보 감사합니다!");

        // when & then
        mockMvc.perform(post("/api/info-posts/{postId}/comments", post.getId())
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("댓글이 작성되었습니다."))
                .andExpect(jsonPath("$.data.postId").value(post.getId()))
                .andExpect(jsonPath("$.data.content").value("좋은 정보 감사합니다!"))
                .andExpect(jsonPath("$.data.authorId").value(owner.getId()))
                .andExpect(jsonPath("$.data.authorName").value("댓글작성자"));

        assertThat(infoCommentRepository.findAll()).hasSize(1);
    }

    @Test
    void 존재하지_않는_게시글에_댓글을_작성하면_404를_반환한다() throws Exception {
        // given
        InfoCommentCreateRequest request = new InfoCommentCreateRequest();
        request.setContent("댓글입니다.");

        // when & then
        mockMvc.perform(post("/api/info-posts/{postId}/comments", 999999L)
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("정보 공유 게시글을 찾을 수 없습니다."));

        assertThat(infoCommentRepository.findAll()).isEmpty();
    }

    @Test
    void 댓글_내용이_비어있으면_400을_반환한다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "댓글 검증 테스트 게시글", "본문입니다.")
        );

        InfoCommentCreateRequest request = new InfoCommentCreateRequest();
        request.setContent("");

        // when & then
        mockMvc.perform(post("/api/info-posts/{postId}/comments", post.getId())
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("댓글 내용은 필수입니다."));

        assertThat(infoCommentRepository.findAll()).isEmpty();
    }

    @Test
    void 작성자는_댓글을_수정할_수_있다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "댓글 수정 테스트 게시글", "본문입니다.")
        );

        InfoComment comment = infoCommentRepository.saveAndFlush(
                new InfoComment(post, owner, "수정 전 댓글입니다.")
        );

        InfoCommentUpdateRequest request = new InfoCommentUpdateRequest();
        request.setContent("수정된 댓글입니다.");

        // when & then
        mockMvc.perform(put("/api/info-posts/{postId}/comments/{commentId}",
                        post.getId(),
                        comment.getId())
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("댓글이 수정되었습니다."))
                .andExpect(jsonPath("$.data.commentId").value(comment.getId()))
                .andExpect(jsonPath("$.data.content").value("수정된 댓글입니다."))
                .andExpect(jsonPath("$.data.authorId").value(owner.getId()));
    }

    @Test
    void 작성자가_아닌_사용자는_댓글을_수정할_수_없다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "댓글 수정 권한 테스트 게시글", "본문입니다.")
        );

        InfoComment comment = infoCommentRepository.saveAndFlush(
                new InfoComment(post, owner, "작성자가 쓴 댓글입니다.")
        );

        InfoCommentUpdateRequest request = new InfoCommentUpdateRequest();
        request.setContent("다른 사용자가 수정하려는 댓글입니다.");

        // when & then
        mockMvc.perform(put("/api/info-posts/{postId}/comments/{commentId}",
                        post.getId(),
                        comment.getId())
                        .param("userId", String.valueOf(otherUser.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("작성자만 댓글을 수정할 수 있습니다."));
    }

    @Test
    void 존재하지_않는_댓글을_수정하면_404를_반환한다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "존재하지 않는 댓글 수정 테스트 게시글", "본문입니다.")
        );

        InfoCommentUpdateRequest request = new InfoCommentUpdateRequest();
        request.setContent("수정 시도");

        // when & then
        mockMvc.perform(put("/api/info-posts/{postId}/comments/{commentId}",
                        post.getId(),
                        999999L)
                        .param("userId", String.valueOf(owner.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다."));
    }

    @Test
    void 작성자는_댓글을_삭제할_수_있다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "댓글 삭제 테스트 게시글", "본문입니다.")
        );

        InfoComment comment = infoCommentRepository.saveAndFlush(
                new InfoComment(post, owner, "삭제할 댓글입니다.")
        );

        // when & then
        mockMvc.perform(delete("/api/info-posts/{postId}/comments/{commentId}",
                        post.getId(),
                        comment.getId())
                        .param("userId", String.valueOf(owner.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("댓글이 삭제되었습니다."));

        assertThat(infoCommentRepository.findById(comment.getId())).isEmpty();
    }

    @Test
    void 관리자는_댓글을_삭제할_수_있다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "관리자 댓글 삭제 테스트 게시글", "본문입니다.")
        );

        InfoComment comment = infoCommentRepository.saveAndFlush(
                new InfoComment(post, owner, "관리자가 삭제할 댓글입니다.")
        );

        // when & then
        mockMvc.perform(delete("/api/info-posts/{postId}/comments/{commentId}",
                        post.getId(),
                        comment.getId())
                        .param("userId", String.valueOf(admin.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글이 삭제되었습니다."));

        assertThat(infoCommentRepository.findById(comment.getId())).isEmpty();
    }

    @Test
    void 작성자도_관리자도_아닌_사용자는_댓글을_삭제할_수_없다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "댓글 삭제 권한 테스트 게시글", "본문입니다.")
        );

        InfoComment comment = infoCommentRepository.saveAndFlush(
                new InfoComment(post, owner, "삭제 권한 테스트 댓글입니다.")
        );

        // when & then
        mockMvc.perform(delete("/api/info-posts/{postId}/comments/{commentId}",
                        post.getId(),
                        comment.getId())
                        .param("userId", String.valueOf(otherUser.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("작성자 또는 관리자만 댓글을 삭제할 수 있습니다."));

        assertThat(infoCommentRepository.findById(comment.getId())).isPresent();
    }

    @Test
    void 존재하지_않는_댓글을_삭제하면_404를_반환한다() throws Exception {
        // given
        InfoPost post = infoPostRepository.saveAndFlush(
                new InfoPost(owner, "존재하지 않는 댓글 삭제 테스트 게시글", "본문입니다.")
        );

        // when & then
        mockMvc.perform(delete("/api/info-posts/{postId}/comments/{commentId}",
                        post.getId(),
                        999999L)
                        .param("userId", String.valueOf(owner.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다."));
    }
}