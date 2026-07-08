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
            description = "족보 수정 요청 JSON 문자열",
            example = """
                    {
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