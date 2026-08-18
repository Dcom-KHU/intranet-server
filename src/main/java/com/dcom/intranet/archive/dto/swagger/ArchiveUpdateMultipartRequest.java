package com.dcom.intranet.archive.dto.swagger;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "족보 수정 multipart/form-data 요청")
public class ArchiveUpdateMultipartRequest {

    @Schema(
            description = "족보 수정 요청 JSON 문자열. 시험 연도/학기/시험 유형을 알 수 없으면 null로 전송합니다. semester=UNKNOWN, examType=ETC도 null로 저장됩니다.",
            example = """
                    {
                      "subjectName": "자료구조",
                      "professorName": "박교수",
                      "examYear": 2024,
                      "semester": "FIRST",
                      "examType": "FINAL",
                      "content": "수정된 족보 내용입니다.",
                      "deleteFileIds": []
                    }
                    """
    )
    private String request;

    @ArraySchema(
            schema = @Schema(
                    description = "새로 추가할 첨부파일",
                    type = "string",
                    format = "binary"
            )
    )
    private List<MultipartFile> files;
}
