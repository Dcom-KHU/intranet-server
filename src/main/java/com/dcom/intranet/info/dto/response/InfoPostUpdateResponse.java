package com.dcom.intranet.info.dto.response;

import com.dcom.intranet.info.domain.InfoPost;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class InfoPostUpdateResponse {

    private final Long postId;
    private final String title;
    private final String content;
    private final LocalDateTime updatedAt;
    private final List<InfoPostFileResponse> files;

    public InfoPostUpdateResponse(InfoPost post) {
        this.postId = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.updatedAt = post.getUpdatedAt();
        this.files = post.getFiles()
                .stream()
                .map(InfoPostFileResponse::new)
                .toList();
    }
}