package com.dcom.intranet.info.dto.response;

import com.dcom.intranet.global.dto.AuthorResponse;
import com.dcom.intranet.info.domain.InfoComment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InfoCommentResponse {

    private final Long commentId;
    private final Long postId;
    private final String content;
    private final AuthorResponse author;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public InfoCommentResponse(InfoComment comment) {
        this.commentId = comment.getId();
        this.postId = comment.getPost().getId();
        this.content = comment.getContent();
        this.author = AuthorResponse.from(comment.getAuthor());
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
    }
}
