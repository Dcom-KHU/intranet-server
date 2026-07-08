package com.dcom.intranet.notice.dto.swagger;

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
@Schema(description = "공지사항 작성 multipart/form-data 요청")
public class NoticeCreateMultipartRequest {

    @Schema(
            description = "공지사항 작성 요청 JSON 문자열",
            example = """
                    {
                      "title": "7월 전체 회의 안내",
                      "content": "7월 전체 회의 일정을 안내합니다."
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
