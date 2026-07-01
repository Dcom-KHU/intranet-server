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
@Schema(description = "족보 등록 multipart/form-data 요청")
public class ArchiveCreateMultipartRequest {

    @Schema(
            description = "족보 등록 요청 JSON 문자열",
            example = """
                    {
                      "subjectName": "자료구조",
                      "professorName": "박교수",
                      "records": [
                        {
                          "examYear": 2024,
                          "semester": "FIRST",
                          "examType": "MIDTERM",
                          "content": "Swagger 테스트 족보입니다.",
                          "fileIndexes": []
                        }
                      ]
                    }
                    """
    )
    private String request;

    @ArraySchema(
            schema = @Schema(
                    description = "첨부파일",
                    type = "string",
                    format = "binary"
            )
    )
    private List<MultipartFile> files;
}