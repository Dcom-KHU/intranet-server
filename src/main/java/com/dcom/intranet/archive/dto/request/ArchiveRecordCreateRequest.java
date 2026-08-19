package com.dcom.intranet.archive.dto.request;

import com.dcom.intranet.archive.domain.ExamType;
import com.dcom.intranet.archive.domain.Semester;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArchiveRecordCreateRequest {

    @Schema(
            description = "시험 연도. 모르면 null로 전송합니다. 빈 문자열은 사용하지 않습니다.",
            example = "2026",
            nullable = true
    )
    private Integer examYear;

    @Schema(
            description = "학기. FIRST, SECOND, SUMMER, WINTER 중 하나이며 모르면 null로 전송합니다. 빈 문자열은 사용하지 않습니다.",
            example = "FIRST",
            allowableValues = {"FIRST", "SECOND", "SUMMER", "WINTER"},
            nullable = true
    )
    private Semester semester;

    @Schema(
            description = "시험 유형. MIDTERM, FINAL, QUIZ, ASSIGNMENT 중 하나이며 모르면 null로 전송합니다. 빈 문자열은 사용하지 않습니다.",
            example = "MIDTERM",
            allowableValues = {"MIDTERM", "FINAL", "QUIZ", "ASSIGNMENT"},
            nullable = true
    )
    private ExamType examType;

    private String content;

    // multipart files 배열에서 이 record에 연결할 파일 index
    // 예: [0, 1]
    private List<Integer> fileIndexes;
}
