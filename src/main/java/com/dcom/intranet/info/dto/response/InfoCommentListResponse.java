package com.dcom.intranet.info.dto.response;

import com.dcom.intranet.info.domain.InfoComment;
import lombok.Getter;

import java.util.List;

@Getter
public class InfoCommentListResponse {

    private final List<InfoCommentResponse> comments;

    public InfoCommentListResponse(List<InfoComment> comments) {
        this.comments = comments.stream()
                .map(InfoCommentResponse::new)
                .toList();
    }
}