package com.dcom.intranet.photo.dto;

import com.dcom.intranet.global.dto.AuthorResponse;
import com.dcom.intranet.photo.domain.PhotoComment;

import java.time.LocalDateTime;

public record PhotoCommentUpdateResponse(
        Long commentId,
        Long albumId,
        AuthorResponse author,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PhotoCommentUpdateResponse from(PhotoComment comment) {
        return new PhotoCommentUpdateResponse(
                comment.getCommentId(),
                comment.getPhotoPost().getAlbumId(),
                AuthorResponse.from(comment.getAuthor()),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
