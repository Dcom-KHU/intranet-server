package com.dcom.intranet.archive.dto.request;

import com.dcom.intranet.archive.domain.ExamType;
import com.dcom.intranet.archive.domain.Semester;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Schema(description = "족보 수정 요청")
@Getter
@Setter
@NoArgsConstructor
public class ArchiveUpdateRequest {

    @Schema(description = "시험 연도", example = "2024")
    @NotNull(message = "시험 연도는 필수입니다.")
    private Integer examYear;

    @Schema(description = "학기", example = "FIRST")
    @NotNull(message = "학기는 필수입니다.")
    private Semester semester;

    @Schema(description = "시험 유형", example = "MIDTERM")
    @NotNull(message = "시험 유형은 필수입니다.")
    private ExamType examType;

    @Schema(description = "족보 설명 또는 본문", example = "2024년 1학기 중간고사 족보입니다.")
    private String content;

    @Schema(description = "삭제할 기존 첨부파일 ID 목록", example = "[1, 2]")
    private List<Long> deleteFileIds;
}