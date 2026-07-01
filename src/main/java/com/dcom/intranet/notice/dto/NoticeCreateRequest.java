package com.dcom.intranet.notice.dto;

import com.dcom.intranet.notice.domain.Notice;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NoticeCreateRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        String content,

        @Valid
        List<FileInfo> files
) {

    public List<Notice.NoticeFile> toNoticeFiles() {
        if (files == null) {
            return List.of();
        }

        return files.stream()
                .map(file -> new Notice.NoticeFile(file.fileName(), file.fileUrl()))
                .toList();
    }

    public record FileInfo(
            @NotBlank
            String fileName,

            @NotBlank
            String fileUrl
    ) {
    }
}
