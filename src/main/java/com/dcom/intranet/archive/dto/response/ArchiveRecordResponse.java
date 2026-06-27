package com.dcom.intranet.archive.dto.response;

import com.dcom.intranet.archive.domain.ArchiveRecord;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ArchiveRecordResponse {

    private final Long recordId;
    private final Integer examYear;
    private final String semester;
    private final String examType;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final ArchiveAuthorResponse author;
    private final List<ArchiveFileResponse> files;

    public ArchiveRecordResponse(ArchiveRecord record) {
        this.recordId = record.getId();
        this.examYear = record.getExamYear();
        this.semester = record.getSemester().name();
        this.examType = record.getExamType().name();
        this.content = record.getContent();
        this.createdAt = record.getCreatedAt();
        this.updatedAt = record.getUpdatedAt();
        this.author = new ArchiveAuthorResponse(record.getAuthor());
        this.files = record.getFiles().stream()
                .map(ArchiveFileResponse::new)
                .toList();
    }
}