package com.dcom.intranet.photo.dto;

import com.dcom.intranet.photo.domain.PhotoPost;

import java.time.LocalDate;
import java.util.List;

public record PhotoPostCreateResponse(
        Long albumId,
        String eventName,
        LocalDate activityDate,
        String place,
        String coverImageUrl,
        List<String> imageUrls
) {

    public static PhotoPostCreateResponse from(PhotoPost photoPost) {
        return new PhotoPostCreateResponse(
                photoPost.getAlbumId(),
                photoPost.getEventName(),
                photoPost.getActivityDate(),
                photoPost.getPlace(),
                photoPost.getImages().isEmpty()
                        ? null
                        : "/api/photo-posts/%d/images/%d".formatted(
                                photoPost.getAlbumId(),
                                photoPost.getImages().get(0).getId()
                        ),
                photoPost.getImages().stream()
                        .map(image -> "/api/photo-posts/%d/images/%d".formatted(photoPost.getAlbumId(), image.getId()))
                        .toList()
        );
    }
}
