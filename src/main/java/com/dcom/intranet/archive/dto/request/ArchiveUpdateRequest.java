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

    @Schema(description = "시험 연도. 학기를 UNKNOWN으로 지정할 때는 null을 허용합니다.", example = "2024")
    private Integer examYear;

    @Schema(description = "학기. FIRST, SECOND, SUMMER, WINTER를 지원합니다. UNKNOWN 또는 null은 null로 저장됩니다.", example = "FIRST")
    private Semester semester;

    @Schema(description = "시험 유형. MIDTERM, FINAL, QUIZ, ASSIGNMENT를 지원합니다. ETC 또는 null은 null로 저장됩니다.", example = "MIDTERM")
    private ExamType examType;

    @Schema(description = "족보 설명 또는 본문", example = "2024년 1학기 중간고사 족보입니다.")
    private String content;

    @Schema(description = "삭제할 기존 첨부파일 ID 목록", example = "[1, 2]")
    private List<Long> deleteFileIds;
}
