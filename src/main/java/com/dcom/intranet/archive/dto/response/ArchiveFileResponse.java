package com.dcom.intranet.archive.dto.response;

import com.dcom.intranet.archive.domain.ArchiveFile;
import lombok.Getter;

@Getter
public class ArchiveFileResponse {

    private final Long fileId;
    private final String originalFileName;
    private final String fileUrl;

    public ArchiveFileResponse(ArchiveFile file) {
        this.fileId = file.getId();
        this.originalFileName = file.getOriginalFileName();
        this.fileUrl = file.getFileUrl();
    }
}