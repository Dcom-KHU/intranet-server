package com.dcom.intranet.archive.dto.response;

import com.dcom.intranet.archive.domain.ArchiveRecord;
import com.dcom.intranet.archive.domain.ExamType;
import com.dcom.intranet.archive.domain.Semester;

public record ArchiveExamInfoResponse(
        Integer examYear,
        String semester,
        String examType,
        String label
) {

    public static ArchiveExamInfoResponse from(ArchiveRecord record) {
        if (record.getExamYear() == null
                || record.getSemester() == null
                || record.getExamType() == null) {
            return null;
        }

        String semesterLabel = toSemesterLabel(record.getSemester());
        String examTypeLabel = toExamTypeLabel(record.getExamType());

        return new ArchiveExamInfoResponse(
                record.getExamYear(),
                record.getSemester().name(),
                record.getExamType().name(),
                "%d년 %s %s".formatted(record.getExamYear(), semesterLabel, examTypeLabel)
        );
    }

    private static String toSemesterLabel(Semester semester) {
        return switch (semester) {
            case FIRST -> "1학기";
            case SECOND -> "2학기";
            case SUMMER -> "여름학기";
            case WINTER -> "겨울학기";
        };
    }

    private static String toExamTypeLabel(ExamType examType) {
        return switch (examType) {
            case MIDTERM -> "중간고사";
            case FINAL -> "기말고사";
            case QUIZ -> "퀴즈";
            case ASSIGNMENT -> "과제";
            case ETC -> "기타";
        };
    }
}
