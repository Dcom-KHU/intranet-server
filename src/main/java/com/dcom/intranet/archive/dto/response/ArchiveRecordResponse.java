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

    private static final char NON_BREAKING_SPACE = '\u00A0';
    private static final String TAB_INDENT = String.valueOf(NON_BREAKING_SPACE).repeat(4);

    private final Long recordId;
    private final Integer examYear;
    private final String semester;
    private final String examType;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final AuthorResponse author;
    private final List<ArchiveFileResponse> files;

    public ArchiveRecordResponse(ArchiveRecord record) {
        this.recordId = record.getId();
        this.examYear = record.getExamYear();
        this.semester = toResponseSemester(record.getSemester());
        this.examType = toResponseExamType(record.getExamType());
        this.content = preserveLineIndentation(record.getContent());
        this.createdAt = record.getCreatedAt();
        this.updatedAt = record.getUpdatedAt();
        this.author = AuthorResponse.fromLegacyOrUser(
                record.getLegacyAuthorStudentNumber(),
                record.getLegacyAuthorName(),
                record.getLegacyAnonymous(),
                record.getAuthor()
        );
        this.files = record.getFiles().stream()
                .map(file -> new ArchiveFileResponse(record.getArchive().getId(), record.getId(), file))
                .toList();
    }

    private String toResponseSemester(Semester semester) {
        if (semester == null || semester == Semester.UNKNOWN) {
            return null;
        }

        return semester.name();
    }

    private String toResponseExamType(ExamType examType) {
        if (examType == null || examType == ExamType.ETC) {
            return null;
        }

        return examType.name();
    }

    private String preserveLineIndentation(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        StringBuilder preserved = new StringBuilder(content.length());
        boolean lineStart = true;

        for (int index = 0; index < content.length(); index++) {
            char current = content.charAt(index);

            if (current == '\n' || current == '\r') {
                preserved.append(current);
                lineStart = true;
                continue;
            }

            if (lineStart && current == ' ') {
                preserved.append(NON_BREAKING_SPACE);
                continue;
            }

            if (lineStart && current == '\t') {
                preserved.append(TAB_INDENT);
                continue;
            }

            preserved.append(current);
            lineStart = false;
        }

        return preserved.toString();
    }
}
