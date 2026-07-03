package com.dcom.intranet.photo.dto;

import com.dcom.intranet.photo.domain.PhotoComment;

import java.time.LocalDateTime;

public record PhotoCommentCreateResponse(
        Long commentId,
        Long albumId,
        Long authorId,
        String content,
        LocalDateTime createdAt
) {

    public static PhotoCommentCreateResponse from(PhotoComment comment) {
        return new PhotoCommentCreateResponse(
                comment.getCommentId(),
                comment.getPhotoPost().getAlbumId(),
                comment.getAuthor().getId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
