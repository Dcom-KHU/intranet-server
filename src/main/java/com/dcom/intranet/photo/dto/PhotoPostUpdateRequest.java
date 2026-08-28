package com.dcom.intranet.photo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record PhotoPostUpdateRequest(
        @NotBlank
        String eventName,

        @NotNull
        LocalDate activityDate,

        String description,

        String place,

        List<Long> deleteFileIds
) {

    public List<Long> deleteImageIds() {
        if (deleteFileIds == null || deleteFileIds.isEmpty()) {
            return List.of();
        }

        return deleteFileIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
