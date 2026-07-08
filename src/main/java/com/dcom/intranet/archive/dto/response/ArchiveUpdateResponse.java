package com.dcom.intranet.archive.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ArchiveUpdateResponse {

    private final Long recordId;
    private final LocalDateTime updatedAt;

    public ArchiveUpdateResponse(Long recordId, LocalDateTime updatedAt) {
        this.recordId = recordId;
        this.updatedAt = updatedAt;
    }
}