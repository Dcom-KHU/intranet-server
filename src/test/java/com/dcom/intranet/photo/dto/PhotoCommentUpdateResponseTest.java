package com.dcom.intranet.photo.dto;

import com.dcom.intranet.auth.domain.User;
import com.dcom.intranet.photo.domain.PhotoComment;
import com.dcom.intranet.photo.domain.PhotoPost;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhotoCommentUpdateResponseTest {

    @Test
    @DisplayName("사진첩 댓글 수정 응답은 프론트 렌더링에 필요한 author와 albumId를 포함한다")
    void updateResponseIncludesAuthorAndAlbumId() {
        User author = new User(
                "login",
                "password",
                "홍길동",
                "20211234",
                "hong@example.com",
                "01012345678"
        );
        PhotoPost photoPost = new PhotoPost(
                author,
                "행사",
                LocalDate.of(2026, 7, 3),
                "설명",
                List.of()
        );
        PhotoComment comment = new PhotoComment(photoPost, author, "수정된 댓글 내용");
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 3, 12, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 3, 13, 0);

        ReflectionTestUtils.setField(photoPost, "albumId", 1L);
        ReflectionTestUtils.setField(comment, "commentId", 10L);
        ReflectionTestUtils.setField(comment, "createdAt", createdAt);
        ReflectionTestUtils.setField(comment, "updatedAt", updatedAt);

        PhotoCommentUpdateResponse response = PhotoCommentUpdateResponse.from(comment);

        assertThat(response.commentId()).isEqualTo(10L);
        assertThat(response.albumId()).isEqualTo(1L);
        assertThat(response.author().studentNumber()).isEqualTo("20211234");
        assertThat(response.author().name()).isEqualTo("홍길동");
        assertThat(response.content()).isEqualTo("수정된 댓글 내용");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }
}
