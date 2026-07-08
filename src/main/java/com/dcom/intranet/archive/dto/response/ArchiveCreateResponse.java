package com.dcom.intranet.archive.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ArchiveCreateResponse {

    private final Long archiveId;
    private final List<Long> recordIds;
    private final LocalDateTime createdAt;

    public ArchiveCreateResponse(Long archiveId, List<Long> recordIds, LocalDateTime createdAt) {
        this.archiveId = archiveId;
        this.recordIds = recordIds;
        this.createdAt = createdAt;
    }
}