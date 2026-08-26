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
                      "place": "Engineering Building 101",
                      "description": "수정된 사진첩 설명입니다.",
                      "deleteFileIds": [2, 3]
                    }
                    """
    )
    private String request;

    @ArraySchema(
            schema = @Schema(
                    description = "추가할 사진 목록. 전달하면 기존 사진 뒤에 추가되며 대표 사진은 기존 첫 번째 사진으로 유지됩니다. 앨범당 최대 10개, 파일당 최대 10MB이며 SVG를 제외한 이미지 파일만 허용합니다.",
                    type = "string",
                    format = "binary"
            )
    )
    private List<MultipartFile> files;
}
