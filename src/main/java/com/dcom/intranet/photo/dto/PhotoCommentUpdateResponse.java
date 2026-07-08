package com.dcom.intranet.photo.dto;

import com.dcom.intranet.photo.domain.PhotoComment;

import java.time.LocalDateTime;

public record PhotoCommentUpdateResponse(
        Long commentId,
        String content,
        LocalDateTime updatedAt
) {

    public static PhotoCommentUpdateResponse from(PhotoComment comment) {
        return new PhotoCommentUpdateResponse(
                comment.getCommentId(),
                comment.getContent(),
                comment.getUpdatedAt()
        );
    }
}
