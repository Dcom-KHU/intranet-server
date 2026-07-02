package com.dcom.intranet.info.dto.response;

import com.dcom.intranet.info.domain.InfoPost;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class InfoPostCreateResponse {

    private final Long postId;
    private final String title;
    private final String content;
    private final Long authorId;
    private final String authorName;
    private final LocalDateTime createdAt;
    private final List<InfoPostFileResponse> files;

    public InfoPostCreateResponse(InfoPost post) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorId = post.getAuthor().getId();
        this.authorName = post.getAuthor().getNickname();
        this.createdAt = post.getCreatedAt();
        this.files = post.getFiles()
                .stream()
                .map(InfoPostFileResponse::new)
                .toList();
    }
}