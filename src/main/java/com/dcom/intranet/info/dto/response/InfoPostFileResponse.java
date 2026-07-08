package com.dcom.intranet.info.dto.response;

import com.dcom.intranet.info.domain.InfoPostFile;
import lombok.Getter;

@Getter
public class InfoPostFileResponse {

    private final Long fileId;
    private final String originalFileName;
    private final String fileUrl;
    private final Long fileSize;
    private final String contentType;

    public InfoPostFileResponse(InfoPostFile file) {
        this.fileId = file.getId();
        this.originalFileName = file.getOriginalFileName();
        this.fileUrl = file.getFileUrl();
        this.fileSize = file.getFileSize();
        this.contentType = file.getContentType();
    }
}