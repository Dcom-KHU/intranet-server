package com.dcom.intranet.info.dto.response;

import com.dcom.intranet.global.dto.AuthorResponse;
import com.dcom.intranet.info.domain.InfoPost;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InfoPostListResponse {

    private final Long postId;
    private final String title;
    private final AuthorResponse author;
    private final LocalDateTime createdAt;
    private final boolean hasFiles;
    private final int fileCount;
    private final int views;

    public InfoPostListResponse(InfoPost post) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.author = AuthorResponse.from(post.getAuthor());
        this.createdAt = post.getCreatedAt();
        this.hasFiles = !post.getFiles().isEmpty();
        this.fileCount = post.getFiles().size();
        this.views = post.getViews();
    }
}
