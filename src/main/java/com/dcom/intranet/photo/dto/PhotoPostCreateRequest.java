package com.dcom.intranet.photo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PhotoPostCreateRequest(
        @NotBlank
        String eventName,

        @NotNull
        LocalDate activityDate,

        String description
) {
}
