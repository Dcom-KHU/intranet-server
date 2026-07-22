package com.dcom.intranet.photo.dto;

import com.dcom.intranet.photo.domain.PhotoPost;

import java.time.LocalDate;
import java.util.List;

public record PhotoPostDetailResponse(
        Long albumId,
        String eventName,
        LocalDate activityDate,
        List<String> imageList,
        String description
) {

    public static PhotoPostDetailResponse from(PhotoPost photoPost) {
        return new PhotoPostDetailResponse(
                photoPost.getAlbumId(),
                photoPost.getEventName(),
                photoPost.getActivityDate(),
                photoPost.getImageUrls(),
                photoPost.getDescription()
        );
    }
}
