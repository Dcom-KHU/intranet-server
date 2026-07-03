package com.dcom.intranet.photo.dto;

import com.dcom.intranet.photo.domain.PhotoPost;

import java.time.LocalDate;
import java.util.List;

public record PhotoPostCreateResponse(
        Long albumId,
        String eventName,
        LocalDate activityDate,
        String coverImageUrl,
        List<String> imageUrls
) {

    public static PhotoPostCreateResponse from(PhotoPost photoPost) {
        return new PhotoPostCreateResponse(
                photoPost.getAlbumId(),
                photoPost.getEventName(),
                photoPost.getActivityDate(),
                photoPost.getCoverImageUrl(),
                photoPost.getImageUrls()
        );
    }
}
