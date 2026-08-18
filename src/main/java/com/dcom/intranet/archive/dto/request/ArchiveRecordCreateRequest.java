package com.dcom.intranet.archive.dto.request;

import com.dcom.intranet.archive.domain.ExamType;
import com.dcom.intranet.archive.domain.Semester;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ArchiveRecordCreateRequest {

    private Integer examYear;

    private Semester semester;

    private ExamType examType;

    private String content;

    // multipart files 배열에서 이 record에 연결할 파일 index
    // 예: [0, 1]
    private List<Integer> fileIndexes;
}
