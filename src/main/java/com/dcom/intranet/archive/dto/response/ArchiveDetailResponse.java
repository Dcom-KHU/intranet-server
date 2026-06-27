package com.dcom.intranet.archive.dto.response;

import com.dcom.intranet.archive.domain.Archive;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;

@Getter
public class ArchiveDetailResponse {

    private final Long archiveId;
    private final String subjectName;
    private final String professorName;
    private final List<ArchiveRecordResponse> records;

    public ArchiveDetailResponse(Archive archive) {
        this.archiveId = archive.getId();
        this.subjectName = archive.getSubjectName();
        this.professorName = archive.getProfessorName();
        this.records = archive.getRecords().stream()
                .sorted(Comparator.comparing(archiveRecord -> archiveRecord.getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())))
                .map(ArchiveRecordResponse::new)
                .toList();
    }
}