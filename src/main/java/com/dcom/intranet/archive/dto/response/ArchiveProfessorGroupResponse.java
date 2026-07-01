package com.dcom.intranet.archive.dto.response;

import com.dcom.intranet.archive.domain.Archive;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ArchiveProfessorGroupResponse {

    private final Long archiveId;
    private final String subjectName;
    private final String professorName;
    private final int recordCount;
    private final LocalDateTime lastModifiedAt;

    public ArchiveProfessorGroupResponse(Archive archive) {
        this.archiveId = archive.getId();
        this.subjectName = archive.getSubjectName();
        this.professorName = archive.getProfessorName();
        this.recordCount = archive.getRecords().size();
        this.lastModifiedAt = archive.getLastModifiedAt();
    }
}