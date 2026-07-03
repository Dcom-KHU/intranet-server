package com.dcom.intranet.photo.dto;

import com.dcom.intranet.photo.domain.PhotoComment;
import com.dcom.intranet.photo.domain.PhotoPost;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PhotoPostDetailResponse(
        Long albumId,
        String eventName,
        LocalDate activityDate,
        List<String> imageList,
        String description,
        List<CommentSummary> comments
) {

    public static PhotoPostDetailResponse from(PhotoPost photoPost) {
        return new PhotoPostDetailResponse(
                photoPost.getAlbumId(),
                photoPost.getEventName(),
                photoPost.getActivityDate(),
                photoPost.getImageUrls(),
                photoPost.getDescription(),
                photoPost.getComments().stream()
                        .map(CommentSummary::from)
                        .toList()
        );
    }

    public record CommentSummary(
            Long commentId,
            Long authorId,
            String authorName,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        public static CommentSummary from(PhotoComment comment) {
            return new CommentSummary(
                    comment.getCommentId(),
                    comment.getAuthor().getId(),
                    comment.getAuthor().getName(),
                    comment.getContent(),
                    comment.getCreatedAt(),
                    comment.getUpdatedAt()
            );
        }
    }
}
