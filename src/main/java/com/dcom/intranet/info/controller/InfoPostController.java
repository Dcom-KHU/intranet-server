package com.dcom.intranet.info.controller;

import com.dcom.intranet.global.response.CommonResponse;
import com.dcom.intranet.global.swagger.SwaggerExamples;
import com.dcom.intranet.info.dto.request.InfoPostCreateRequest;
import com.dcom.intranet.info.dto.request.InfoPostUpdateRequest;
import com.dcom.intranet.info.dto.response.InfoPostCreateResponse;
import com.dcom.intranet.info.dto.response.InfoPostDetailResponse;
import com.dcom.intranet.info.dto.response.InfoPostPageResponse;
import com.dcom.intranet.info.dto.response.InfoPostUpdateResponse;
import com.dcom.intranet.info.service.InfoPostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Tag(name = "Information Board", description = "정보공유 게시판 API")
@RestController
@RequestMapping("/api/info-posts")
@RequiredArgsConstructor
public class InfoPostController {

    private final InfoPostService infoPostService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(
            summary = "정보공유 게시글 목록 조회",
            description = """
                    정보공유 게시글 목록을 조회합니다.
                    
                    keyword가 있으면 제목 또는 본문 기준으로 검색합니다.
                    sort 값은 latest, oldest, views를 지원합니다.
                    기본 정렬은 latest입니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_LIST_SUCCESS_200)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.UNAUTHORIZED_401)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<CommonResponse<InfoPostPageResponse>> getPosts(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "검색어. 제목 또는 본문 기준 검색", example = "시간 복잡도")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "정렬 기준. latest, oldest, views", example = "latest")
            @RequestParam(defaultValue = "latest") String sort
    ) {
        InfoPostPageResponse response =
                infoPostService.getPosts(page, size, keyword, sort);

        return ResponseEntity.ok(
                CommonResponse.success(response)
        );
    }

    @Operation(
            summary = "정보공유 게시글 상세 조회",
            description = "특정 정보공유 게시글의 상세 정보를 조회합니다. 상세 조회 시 조회수가 증가합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 상세 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_DETAIL_SUCCESS_200)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.UNAUTHORIZED_401)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND_404)
                    )
            )
    })
    @GetMapping("/{postId}")
    public ResponseEntity<CommonResponse<InfoPostDetailResponse>> getPostDetail(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId
    ) {
        InfoPostDetailResponse response =
                infoPostService.getPostDetail(postId);

        return ResponseEntity.ok(
                CommonResponse.success(response)
        );
    }

    @Operation(
            summary = "정보공유 게시글 작성",
            description = """
                정보공유 게시글을 작성합니다.

                request 파트에는 JSON 문자열을 넣고,
                files 파트에는 첨부파일을 넣습니다.
                files는 선택입니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "게시글 작성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_CREATE_SUCCESS_201)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_BAD_REQUEST_400)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 필요",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.UNAUTHORIZED_401)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_NOT_FOUND_404)
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<InfoPostCreateResponse>> createPost(
            @RequestPart("request") String requestJson,

            @Parameter(description = "첨부파일 목록")
            @RequestPart(value = "files", required = false) List<MultipartFile> files,

            @Parameter(description = "요청 사용자 ID. JWT 적용 전 임시 파라미터", example = "1")
            @RequestParam Long userId
    ) {
        InfoPostCreateRequest request = parseCreateRequest(requestJson);

        InfoPostCreateResponse response =
                infoPostService.createPost(request, files, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(
                        201,
                        "게시글이 작성되었습니다.",
                        response
                ));
    }

    private InfoPostCreateRequest parseCreateRequest(String requestJson) {
        try {
            InfoPostCreateRequest request =
                    objectMapper.readValue(requestJson, InfoPostCreateRequest.class);

            return validateRequest(request);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "request JSON 형식이 올바르지 않습니다."
            );
        }
    }

    private <T> T validateRequest(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            String message = violations.iterator()
                    .next()
                    .getMessage();

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    message
            );
        }

        return request;
    }

    @Operation(
            summary = "정보공유 게시글 수정",
            description = """
                정보공유 게시글을 수정합니다.

                작성자만 수정할 수 있습니다.
                request 파트에는 JSON 문자열을 넣고,
                files 파트에는 새로 추가할 첨부파일을 넣습니다.
                기존 파일 중 삭제할 파일은 deleteFileIds로 전달합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 수정 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_UPDATE_SUCCESS_200)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_BAD_REQUEST_400)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "수정 권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_UPDATE_FORBIDDEN_403)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 또는 사용자 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_NOT_FOUND_404)
                    )
            )
    })
    @PutMapping(
            value = "/{postId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CommonResponse<InfoPostUpdateResponse>> updatePost(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,

            @RequestPart("request") String requestJson,

            @Parameter(description = "새로 추가할 첨부파일 목록")
            @RequestPart(value = "files", required = false) List<MultipartFile> files,

            @Parameter(description = "요청 사용자 ID. JWT 적용 전 임시 파라미터", example = "1")
            @RequestParam Long userId
    ) {
        InfoPostUpdateRequest request = parseUpdateRequest(requestJson);

        InfoPostUpdateResponse response =
                infoPostService.updatePost(postId, request, files, userId);

        return ResponseEntity.ok(
                CommonResponse.success(
                        200,
                        "게시글이 수정되었습니다.",
                        response
                )
        );
    }

    private InfoPostUpdateRequest parseUpdateRequest(String requestJson) {
        try {
            InfoPostUpdateRequest request =
                    objectMapper.readValue(requestJson, InfoPostUpdateRequest.class);

            return validateRequest(request);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "request JSON 형식이 올바르지 않습니다."
            );
        }
    }

    @Operation(
            summary = "정보공유 게시글 삭제",
            description = """
                정보공유 게시글을 삭제합니다.

                작성자 또는 ADMIN만 삭제할 수 있습니다.
                삭제는 물리 삭제로 처리하며, 게시글 DB row와 첨부파일 메타데이터,
                로컬 저장소의 실제 첨부파일을 함께 삭제합니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "게시글 삭제 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_DELETE_SUCCESS_200)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "삭제 권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_DELETE_FORBIDDEN_403)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글 또는 사용자 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_NOT_FOUND_404)
                    )
            )
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<CommonResponse<Void>> deletePost(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,

            @Parameter(description = "요청 사용자 ID. JWT 적용 전 임시 파라미터", example = "1")
            @RequestParam Long userId
    ) {
        infoPostService.deletePost(postId, userId);

        return ResponseEntity.ok(
                CommonResponse.success(
                        200,
                        "게시글이 삭제되었습니다."
                )
        );
    }
}