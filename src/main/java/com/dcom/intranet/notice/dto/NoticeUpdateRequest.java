package com.dcom.intranet.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NoticeUpdateRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        String content,

        List<Long> deleteFileIds
) {
}
