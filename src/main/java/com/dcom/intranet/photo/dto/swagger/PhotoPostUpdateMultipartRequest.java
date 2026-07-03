package com.dcom.intranet.photo.dto.swagger;

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
@Schema(description = "사진첩 수정 multipart/form-data 요청")
public class PhotoPostUpdateMultipartRequest {

    @Schema(
            description = "사진첩 수정 요청 JSON 문자열",
            example = """
                    {
                      "eventName": "신입생 환영회 수정",
                      "activityDate": "2026-07-04",
                      "description": "수정된 사진첩 설명입니다."
                    }
                    """
    )
    private String request;

    @ArraySchema(
            schema = @Schema(
                    description = "새 사진 목록. 전달하면 기존 사진 전체를 교체하며 첫 번째 사진이 대표 사진으로 사용됩니다.",
                    type = "string",
                    format = "binary"
            )
    )
    private List<MultipartFile> files;
}
