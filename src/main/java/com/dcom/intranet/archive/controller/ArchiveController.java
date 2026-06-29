package com.dcom.intranet.archive.controller;

import com.dcom.intranet.archive.dto.request.ArchiveCreateRequest;
import com.dcom.intranet.archive.dto.request.ArchiveUpdateRequest;
import com.dcom.intranet.archive.dto.response.ArchiveUpdateResponse;
import com.dcom.intranet.archive.dto.response.*;
import com.dcom.intranet.archive.dto.swagger.ArchiveCreateMultipartRequest;
import com.dcom.intranet.archive.dto.swagger.ArchiveUpdateMultipartRequest;
import com.dcom.intranet.archive.service.ArchiveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "Archive", description = "족보 아카이브 API")
@RestController
@RequestMapping("/api/archives")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "족보 목록 조회", description = "최근 수정일 기준으로 족보 아카이브 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ArchivePageResponse<ArchiveListResponse>> getArchives(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(archiveService.getArchives(page, size));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchArchives(
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String professorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (subjectName != null && !subjectName.isBlank()) {
            return ResponseEntity.ok(
                    archiveService.searchBySubjectName(subjectName, page, size)
            );
        }

        if (professorName != null && !professorName.isBlank()) {
            return ResponseEntity.ok(
                    archiveService.searchByProfessorName(professorName, page, size)
            );
        }

        throw new IllegalArgumentException("subjectName 또는 professorName 중 하나는 필요합니다.");
    }

    @GetMapping("/{archiveId}")
    public ResponseEntity<ArchiveDetailResponse> getArchiveDetail(
            @PathVariable Long archiveId
    ) {
        return ResponseEntity.ok(archiveService.getArchiveDetail(archiveId));
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
            @ApiResponse(responseCode = "201", description = "족보 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 아카이브 없음")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArchiveCreateResponse> createArchive(
            @RequestPart("request") String requestJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(description = "요청 사용자 ID. JWT 적용 전 임시 파라미터", example = "1")
            @RequestParam Long userId
    ) {
        ArchiveCreateRequest request = parseCreateRequest(requestJson);

        ArchiveCreateResponse response = archiveService.createArchive(request, files, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private ArchiveCreateRequest parseCreateRequest(String requestJson) {
        try {
            return objectMapper.readValue(requestJson, ArchiveCreateRequest.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "request JSON 형식이 올바르지 않습니다."
            );
        }
    }

    @Operation(
            summary = "족보 수정",
            description = """
                족보 레코드를 수정합니다.

                request 파트에는 JSON 문자열을 넣고, files 파트에는 새로 추가할 첨부파일을 넣습니다.
                기존 파일 중 삭제할 파일은 request.deleteFileIds에 담습니다.
                files가 있으면 기존 파일을 유지한 상태에서 새 파일이 추가됩니다.
                """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
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
            @ApiResponse(responseCode = "200", description = "족보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "족보 또는 사용자 없음")
    })
    @PutMapping(
            value = "/{archiveId}/records/{recordId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ArchiveUpdateResponse> updateRecord(
            @Parameter(description = "아카이브 ID", example = "1")
            @PathVariable Long archiveId,

            @Parameter(description = "족보 레코드 ID", example = "1")
            @PathVariable Long recordId,

            @RequestPart("request") String requestJson,

            @RequestPart(value = "files", required = false) List<MultipartFile> files,

            @Parameter(description = "요청 사용자 ID. JWT 적용 전 임시 파라미터", example = "1")
            @RequestParam Long userId
    ) {
        ArchiveUpdateRequest request = parseUpdateRequest(requestJson);

        ArchiveUpdateResponse response =
                archiveService.updateRecord(archiveId, recordId, request, files, userId);

        return ResponseEntity.ok(response);
    }

    private ArchiveUpdateRequest parseUpdateRequest(String requestJson) {
        try {
            return objectMapper.readValue(requestJson, ArchiveUpdateRequest.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "request JSON 형식이 올바르지 않습니다."
            );
        }
    }

    @Operation(
            summary = "족보 삭제",
            description = "작성자 또는 관리자가 족보 레코드를 삭제합니다. 연결된 첨부파일 DB 데이터와 로컬 저장소 파일도 함께 삭제됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "족보 또는 사용자 없음")
    })
    @DeleteMapping("/{archiveId}/records/{recordId}")
    public ResponseEntity<ArchiveMessageResponse> deleteRecord(
            @Parameter(description = "아카이브 ID", example = "1")
            @PathVariable Long archiveId,

            @Parameter(description = "족보 레코드 ID", example = "10")
            @PathVariable Long recordId,

            @Parameter(description = "요청 사용자 ID. JWT 적용 전 임시 파라미터", example = "1")
            @RequestParam Long userId
    ) {
        archiveService.deleteRecord(archiveId, recordId, userId);

        return ResponseEntity.ok(
                new ArchiveMessageResponse("족보가 삭제되었습니다.")
        );
    }

    @Operation(
            summary = "족보 파일 다운로드",
            description = "특정 족보 레코드에 첨부된 파일을 다운로드합니다. MVP에서는 다운로드 횟수는 증가시키지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
            @ApiResponse(responseCode = "404", description = "아카이브, 족보 레코드 또는 파일을 찾을 수 없음")
    })
    @GetMapping("/{archiveId}/records/{recordId}/files/{fileId}/download")
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
}