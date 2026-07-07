package com.dcom.intranet.photo.dto;

import com.dcom.intranet.global.dto.AuthorResponse;
import com.dcom.intranet.photo.domain.PhotoComment;

import java.time.LocalDateTime;

public record PhotoCommentCreateResponse(
        Long commentId,
        Long albumId,
        AuthorResponse author,
        String content,
        LocalDateTime createdAt
) {

    public static PhotoCommentCreateResponse from(PhotoComment comment) {
        return new PhotoCommentCreateResponse(
                comment.getCommentId(),
                comment.getPhotoPost().getAlbumId(),
                AuthorResponse.from(comment.getAuthor()),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
