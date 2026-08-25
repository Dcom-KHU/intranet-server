package com.dcom.intranet.archive.dto.request;

import com.dcom.intranet.archive.domain.ExamType;
import com.dcom.intranet.archive.domain.Semester;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Schema(description = "족보 수정 요청")
@Getter
@Setter
@NoArgsConstructor
public class ArchiveUpdateRequest {

    @Schema(description = "수정할 과목명. 생략하면 기존 값을 유지합니다.", example = "자료구조")
    private String subjectName;

    @Schema(description = "수정할 교수명. 생략하면 기존 값을 유지합니다.", example = "박교수")
    private String professorName;

    @Schema(description = "시험 연도. 모르면 null로 전송합니다. 빈 문자열은 사용하지 않습니다.", example = "2024", nullable = true)
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

    @Schema(description = "족보 설명 또는 본문", example = "2024년 1학기 중간고사 족보입니다.")
    private String content;

    @Schema(description = "삭제할 기존 첨부파일 ID 목록", example = "[1, 2]")
    private List<Long> deleteFileIds;
}
