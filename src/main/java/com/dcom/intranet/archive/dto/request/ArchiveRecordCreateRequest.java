package com.dcom.intranet.archive.dto.request;

import com.dcom.intranet.archive.domain.ExamType;
import com.dcom.intranet.archive.domain.Semester;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArchiveRecordCreateRequest {

    @NotNull(message = "시험 연도는 필수입니다.")
    private Integer examYear;

    @NotNull(message = "학기는 필수입니다.")
    private Semester semester;

    @NotNull(message = "시험 유형은 필수입니다.")
    private ExamType examType;

    private String content;

    // multipart files 배열에서 이 record에 연결할 파일 index
    // 예: [0, 1]
    private List<Integer> fileIndexes;
}