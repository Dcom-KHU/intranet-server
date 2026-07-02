package com.dcom.intranet.archive.dto.response;

import com.dcom.intranet.archive.domain.Archive;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ArchiveListResponse {

    private Long archiveId;
    private String subjectName;
    private String professorName;
    private int recordCount;
    private LocalDateTime lastModifiedAt;

    public ArchiveListResponse(Archive archive) {
        this.archiveId = archive.getId();
        this.subjectName = archive.getSubjectName();
        this.professorName = archive.getProfessorName();
        // size()는 N+1 문제가 발생할 수 있음. 나중에 count query로 최적화 예정
        this.recordCount = archive.getRecords().size();
        this.lastModifiedAt = archive.getLastModifiedAt();
    }
}