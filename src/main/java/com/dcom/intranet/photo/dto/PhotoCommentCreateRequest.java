package com.dcom.intranet.photo.dto;

import jakarta.validation.constraints.NotBlank;

public record PhotoCommentCreateRequest(
        @NotBlank
        String content
) {
}
