package com.dcom.intranet.archive.dto.response;

import com.dcom.intranet.archive.domain.ArchiveRecord;
import com.dcom.intranet.archive.domain.ExamType;
import com.dcom.intranet.archive.domain.Semester;
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
    private final String label;
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
        this.label = createLabel(record);
        this.content = record.getContent();
        this.createdAt = record.getCreatedAt();
        this.updatedAt = record.getUpdatedAt();
        this.author = AuthorResponse.from(record.getAuthor());
        this.files = record.getFiles().stream()
                .map(file -> new ArchiveFileResponse(record.getArchive().getId(), record.getId(), file))
                .toList();
    }

    private String createLabel(ArchiveRecord record) {
        if (record.getExamYear() == null
                || record.getSemester() == null
                || record.getExamType() == null) {
            return null;
        }

        return "%d년 %s %s".formatted(
                record.getExamYear(),
                toSemesterLabel(record.getSemester()),
                toExamTypeLabel(record.getExamType())
        );
    }

    private String toSemesterLabel(Semester semester) {
        return switch (semester) {
            case FIRST -> "1학기";
            case SECOND -> "2학기";
            case SUMMER -> "여름학기";
            case WINTER -> "겨울학기";
        };
    }

    private String toExamTypeLabel(ExamType examType) {
        return switch (examType) {
            case MIDTERM -> "중간고사";
            case FINAL -> "기말고사";
            case QUIZ -> "퀴즈";
            case ASSIGNMENT -> "과제";
            case ETC -> "기타";
        };
    }
}
