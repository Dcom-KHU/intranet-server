package com.dcom.intranet.archive.dto.response;

import com.dcom.intranet.archive.domain.ArchiveFile;
import lombok.Getter;

@Getter
public class ArchiveFileResponse {

    private final Long fileId;
    private final String originalFileName;
    private final String fileUrl;

    public ArchiveFileResponse(Long archiveId, Long recordId, ArchiveFile file) {
        this.fileId = file.getId();
        this.originalFileName = file.getOriginalFileName();
        this.fileUrl = "/api/archives/%d/records/%d/files/%d/download"
                .formatted(archiveId, recordId, file.getId());
    }
}
