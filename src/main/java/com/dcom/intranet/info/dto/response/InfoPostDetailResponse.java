package com.dcom.intranet.info.dto.response;

import com.dcom.intranet.global.dto.AuthorResponse;
import com.dcom.intranet.info.domain.InfoPost;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class InfoPostDetailResponse {

    private final Long postId;
    private final String title;
    private final String content;
    private final AuthorResponse author;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final int views;
    private final List<InfoPostFileResponse> files;

    public InfoPostDetailResponse(InfoPost post) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.author = AuthorResponse.from(post.getAuthor());
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.views = post.getViews();
        this.files = post.getFiles()
                .stream()
                .map(InfoPostFileResponse::new)
                .toList();
    }
}
