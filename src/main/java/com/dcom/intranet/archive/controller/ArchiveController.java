package com.dcom.intranet.archive.controller;

import com.dcom.intranet.archive.dto.request.ArchiveCreateRequest;
import com.dcom.intranet.archive.dto.request.ArchiveUpdateRequest;
import com.dcom.intranet.archive.dto.response.ArchiveCreateResponse;
import com.dcom.intranet.archive.dto.response.ArchiveDetailResponse;
import com.dcom.intranet.archive.dto.response.ArchiveListResponse;
import com.dcom.intranet.archive.dto.response.ArchivePageResponse;
import com.dcom.intranet.archive.dto.response.ArchiveUpdateResponse;
import com.dcom.intranet.archive.dto.swagger.ArchiveCreateMultipartRequest;
import com.dcom.intranet.archive.dto.swagger.ArchiveUpdateMultipartRequest;
import com.dcom.intranet.archive.service.ArchiveService;
import com.dcom.intranet.global.response.CommonResponse;
import com.dcom.intranet.global.swagger.SwaggerExamples;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Tag(name = "Archive", description = "족보 아카이브 API")
@RestController
@RequestMapping("/api/archives")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(
            summary = "족보 목록 조회",
            description = "최근 수정일 기준으로 족보 아카이브 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping
    public ResponseEntity<CommonResponse<ArchivePageResponse<ArchiveListResponse>>> getArchives(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        ArchivePageResponse<ArchiveListResponse> response =
                archiveService.getArchives(page, size);

        return ResponseEntity.ok(
                CommonResponse.success(response)
        );
    }

    @Operation(
            summary = "족보 검색",
            description = "과목명 또는 교수명으로 족보 아카이브를 검색합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.SUCCESS_200)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "검색 조건 누락",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.BAD_REQUEST_400)
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
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<?>> searchArchives(
            @Parameter(description = "과목명", example = "자료구조")
            @RequestParam(required = false) String subjectName,

            @Parameter(description = "교수명", example = "박교수")
            @RequestParam(required = false) String professorName,

            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        if (subjectName != null && !subjectName.isBlank()) {
            return ResponseEntity.ok(
                    CommonResponse.success(
                            archiveService.searchBySubjectName(subjectName, page, size)
                    )
            );
        }

        if (professorName != null && !professorName.isBlank()) {
            return ResponseEntity.ok(
                    CommonResponse.success(
                            archiveService.searchByProfessorName(professorName, page, size)
                    )
            );
        }

        throw new IllegalArgumentException("subjectName 또는 professorName 중 하나는 필요합니다.");
    }

    @Operation(
            summary = "족보 상세 조회",
            description = "특정 족보 아카이브의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "상세 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.ARCHIVE_DETAIL_SUCCESS_200)
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
                    description = "아카이브 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND_404)
                    )
            )
    })
    @GetMapping("/{archiveId}")
    public ResponseEntity<CommonResponse<ArchiveDetailResponse>> getArchiveDetail(
            @Parameter(description = "아카이브 ID", example = "1")
            @PathVariable Long archiveId
    ) {
        ArchiveDetailResponse response = archiveService.getArchiveDetail(archiveId);

        return ResponseEntity.ok(
                CommonResponse.success(response)
        );
    }

    @Operation(
            summary = "족보 등록",
            description = """
                    족보를 등록합니다.

                    메인 화면 등록 시 subjectName, professorName을 사용합니다.
                    상세 화면 등록 시 archiveId를 사용합니다.
                    request 파트에는 JSON 문자열을 넣고, files 파트에는 첨부파일을 넣습니다.
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ArchiveCreateMultipartRequest.class),
                            encoding = {
                                    @Encoding(
                                            name = "request",
                                            contentType = MediaType.APPLICATION_JSON_VALUE
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "족보 등록 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.CREATED_201)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.BAD_REQUEST_400)
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
                    description = "사용자 또는 아카이브 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND_404)
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<ArchiveCreateResponse>> createArchive(
            @RequestPart("request") String requestJson,

            @Parameter(description = "첨부파일 목록")
            @RequestPart(value = "files", required = false) List<MultipartFile> files,

            @Parameter(description = "요청 사용자 ID. JWT 적용 전 임시 파라미터", example = "1")
            Authentication authentication
    ) {
        ArchiveCreateRequest request = parseCreateRequest(requestJson);

        ArchiveCreateResponse response =
                archiveService.createArchive(
                        request,
                        files,
                        authentication.getName()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        CommonResponse.success(
                                201,
                                "족보가 등록되었습니다.",
                                response
                        )
                );
    }

    @Operation(
            summary = "족보 수정",
            description = """
                    족보 레코드를 수정합니다.

                    request 파트에는 JSON 문자열을 넣고, files 파트에는 새로 추가할 첨부파일을 넣습니다.
                    기존 파일 중 삭제할 파일은 request.deleteFileIds에 담습니다.
                    files가 있으면 기존 파일을 유지한 상태에서 새 파일이 추가됩니다.
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ArchiveUpdateMultipartRequest.class),
                            encoding = {
                                    @Encoding(
                                            name = "request",
                                            contentType = MediaType.APPLICATION_JSON_VALUE
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "족보 수정 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.UPDATE_SUCCESS_200)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.BAD_REQUEST_400)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "수정 권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.FORBIDDEN_403)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "족보 또는 사용자 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND_404)
                    )
            )
    })
    @PutMapping(
            value = "/{archiveId}/records/{recordId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CommonResponse<ArchiveUpdateResponse>> updateRecord(
            @Parameter(description = "아카이브 ID", example = "1")
            @PathVariable Long archiveId,

            @Parameter(description = "족보 레코드 ID", example = "10")
            @PathVariable Long recordId,

            @RequestPart("request") String requestJson,

            @Parameter(description = "새로 추가할 첨부파일 목록")
            @RequestPart(value = "files", required = false) List<MultipartFile> files,

            @Parameter(description = "요청 사용자 ID. JWT 적용 전 임시 파라미터", example = "1")
            Authentication authentication

    ) {
        ArchiveUpdateRequest request = parseUpdateRequest(requestJson);

        ArchiveUpdateResponse response =
                archiveService.updateRecord(
                        archiveId,
                        recordId,
                        request,
                        files,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                CommonResponse.success(
                        200,
                        "족보가 수정되었습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "족보 삭제",
            description = "작성자 또는 관리자가 족보 레코드를 삭제합니다. 연결된 첨부파일 DB 데이터와 로컬 저장소 파일도 함께 삭제됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.DELETE_SUCCESS_200)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "삭제 권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.FORBIDDEN_403)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "족보 또는 사용자 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND_404)
                    )
            )
    })
    @DeleteMapping("/{archiveId}/records/{recordId}")
    public ResponseEntity<CommonResponse<Void>> deleteRecord(
            @Parameter(description = "아카이브 ID", example = "1")
            @PathVariable Long archiveId,

            @Parameter(description = "족보 레코드 ID", example = "10")
            @PathVariable Long recordId,

            @Parameter(description = "요청 사용자 ID. JWT 적용 전 임시 파라미터", example = "1")
            Authentication authentication

    ) {
        archiveService.deleteRecord(
                archiveId,
                recordId,
                authentication.getName()
        );

        return ResponseEntity.ok(
                CommonResponse.success(
                        200,
                        "족보가 삭제되었습니다."
                )
        );
    }

    @Operation(
            summary = "족보 파일 다운로드",
            description = "특정 족보 레코드에 첨부된 파일을 다운로드합니다. 파일 다운로드 API는 JSON 공통 응답이 아닌 파일 바이너리 응답을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "파일 다운로드 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "아카이브, 족보 레코드 또는 파일을 찾을 수 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.NOT_FOUND_404)
                    )
            )
    })
    @GetMapping(
            value = "/{archiveId}/records/{recordId}/files/{fileId}/download",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "아카이브 ID", example = "1")
            @PathVariable Long archiveId,

            @Parameter(description = "족보 레코드 ID", example = "10")
            @PathVariable Long recordId,

            @Parameter(description = "첨부파일 ID", example = "3")
            @PathVariable Long fileId
    ) {
        ArchiveService.DownloadFile downloadFile =
                archiveService.downloadFile(archiveId, recordId, fileId);

        String contentType = downloadFile.getContentType() != null
                ? downloadFile.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(downloadFile.getFileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(downloadFile.getResource());
    }

    private ArchiveCreateRequest parseCreateRequest(String requestJson) {
        try {
            ArchiveCreateRequest request =
                    objectMapper.readValue(requestJson, ArchiveCreateRequest.class);

            return validateRequest(request);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "request JSON 형식이 올바르지 않습니다."
            );
        }
    }

    private ArchiveUpdateRequest parseUpdateRequest(String requestJson) {
        try {
            ArchiveUpdateRequest request =
                    objectMapper.readValue(requestJson, ArchiveUpdateRequest.class);

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
}