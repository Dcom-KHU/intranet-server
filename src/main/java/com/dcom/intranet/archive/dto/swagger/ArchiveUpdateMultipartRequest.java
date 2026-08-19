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
            description = "족보 수정 요청 JSON 문자열. examYear, semester, examType은 각각 선택값입니다. 모르면 null로 전송하며 빈 문자열이나 UNKNOWN/ETC 값은 사용하지 않습니다. 예: \"examYear\": null, \"semester\": null, \"examType\": null",
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
