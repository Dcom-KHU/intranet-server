package com.dcom.intranet.archive.dto.response;

import com.dcom.intranet.archive.domain.ArchiveRecord;
import com.dcom.intranet.global.dto.AuthorResponse;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ArchiveRecordResponse {

    private final Long recordId;
    private final Integer examYear;
    private final String semester;
    private final String examType;
    private final ArchiveExamInfoResponse examInfo;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final AuthorResponse author;
    private final List<ArchiveFileResponse> files;

    public ArchiveRecordResponse(ArchiveRecord record) {
        this.recordId = record.getId();
        this.examYear = record.getExamYear();
        this.semester = record.getSemester() == null ? null : record.getSemester().name();
        this.examType = record.getExamType() == null ? null : record.getExamType().name();
        this.examInfo = ArchiveExamInfoResponse.from(record);
        this.content = record.getContent();
        this.createdAt = record.getCreatedAt();
        this.updatedAt = record.getUpdatedAt();
        this.author = AuthorResponse.from(record.getAuthor());
        this.files = record.getFiles().stream()
                .map(file -> new ArchiveFileResponse(record.getArchive().getId(), record.getId(), file))
                .toList();
    }
}
