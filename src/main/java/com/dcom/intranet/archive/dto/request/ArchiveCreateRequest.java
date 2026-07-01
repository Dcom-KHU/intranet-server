package com.dcom.intranet.archive.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Schema(description = "족보 등록 요청")
@Getter
@Setter
@NoArgsConstructor
public class ArchiveCreateRequest {

    @Schema(description = "상세 페이지에서 등록할 때 사용하는 아카이브 ID", example = "1")
    // 상세 페이지에서 등록할 때 사용
    private Long archiveId;

    // 메인 페이지에서 등록할 때 사용
    @Schema(description = "메인 페이지에서 등록할 때 입력하는 과목명", example = "자료구조")
    private String subjectName;

    @Schema(description = "메인 페이지에서 등록할 때 입력하는 교수명", example = "박교수")
    private String professorName;

    @Valid
    @NotEmpty(message = "족보 레코드는 최소 1개 이상 필요합니다.")
    private List<ArchiveRecordCreateRequest> records;
}