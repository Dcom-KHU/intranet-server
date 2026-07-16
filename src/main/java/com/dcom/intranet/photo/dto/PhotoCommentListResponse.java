package com.dcom.intranet.photo.dto;

import com.dcom.intranet.global.dto.AuthorResponse;
import com.dcom.intranet.photo.domain.PhotoComment;

import java.time.LocalDateTime;
import java.util.List;

public record PhotoCommentListResponse(
        List<CommentSummary> comments
) {

    public static PhotoCommentListResponse from(List<PhotoComment> comments) {
        return new PhotoCommentListResponse(
                comments.stream()
                        .map(CommentSummary::from)
                        .toList()
        );
    }

    public record CommentSummary(
            Long commentId,
            Long albumId,
            AuthorResponse author,
            String content,
            LocalDateTime createdAt
    ) {

        public static CommentSummary from(PhotoComment comment) {
            return new CommentSummary(
                    comment.getCommentId(),
                    comment.getPhotoPost().getAlbumId(),
                    AuthorResponse.from(comment.getAuthor()),
                    comment.getContent(),
                    comment.getCreatedAt()
            );
        }
    }
}
