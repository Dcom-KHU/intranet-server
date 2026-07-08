package com.dcom.intranet.notice.controller;

import com.dcom.intranet.global.response.CommonResponse;
import com.dcom.intranet.notice.dto.NoticeCreateRequest;
import com.dcom.intranet.notice.dto.NoticeCreateResponse;
import com.dcom.intranet.notice.dto.NoticeDeleteResponse;
import com.dcom.intranet.notice.dto.NoticeDetailResponse;
import com.dcom.intranet.notice.dto.NoticeListResponse;
import com.dcom.intranet.notice.dto.NoticeUpdateRequest;
import com.dcom.intranet.notice.dto.NoticeUpdateResponse;
import com.dcom.intranet.notice.dto.swagger.NoticeCreateMultipartRequest;
import com.dcom.intranet.notice.dto.swagger.NoticeUpdateMultipartRequest;
import com.dcom.intranet.notice.service.NoticeService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
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

import java.util.List;
import java.util.Set;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Tag(name = "공지사항", description = "공지사항 API")
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private static final String NOTICE_LIST_SUCCESS_200_EXAMPLE = """
            {
              "success": true,
              "status": 200,
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": {
                "noticeList": [
                  {
                    "noticeId": 1,
                    "title": "7월 전체 회의 안내",
                    "author": {
                      "studentNumber": "20201234",
                      "name": "표지훈"
                    },
                    "createdAt": "2026-07-03T12:00:00"
                  }
                ],
                "pageInfo": {
                  "page": 0,
                  "size": 10,
                  "totalElements": 1,
                  "totalPages": 1,
                  "first": true,
                  "last": true
                }
              }
            }
            """;

    private static final String NOTICE_DETAIL_SUCCESS_200_EXAMPLE = """
            {
              "success": true,
              "status": 200,
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": {
                "noticeId": 1,
                "title": "7월 전체 회의 안내",
                "content": "7월 전체 회의 일정을 안내합니다.",
                "author": {
                  "studentNumber": "20201234",
                  "name": "표지훈"
                },
                "createdAt": "2026-07-03T12:00:00",
                "files": [
                  {
                    "fileId": 1,
                    "originalFileName": "notice.pdf",
                    "fileUrl": "/uploads/notice/2026/07/notice.pdf"
                  }
                ]
              }
            }
            """;

    private static final String NOTICE_CREATE_SUCCESS_201_EXAMPLE = """
            {
              "success": true,
              "status": 201,
              "message": "공지사항이 작성되었습니다.",
              "data": {
                "noticeId": 1,
                "title": "7월 전체 회의 안내",
                "content": "7월 전체 회의 일정을 안내합니다.",
                "createdAt": "2026-07-03T12:00:00"
              }
            }
            """;

    private static final String NOTICE_UPDATE_SUCCESS_200_EXAMPLE = """
            {
              "success": true,
              "status": 200,
              "message": "공지사항이 수정되었습니다.",
              "data": {
                "noticeId": 1,
                "title": "7월 전체 회의 안내 수정",
                "updatedAt": "2026-07-03T13:00:00"
              }
            }
            """;

    private static final String NOTICE_DELETE_SUCCESS_200_EXAMPLE = """
            {
              "success": true,
              "status": 200,
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": {
                "message": "공지사항이 삭제되었습니다."
              }
            }
            """;

    private static final String BAD_REQUEST_400_EXAMPLE = """
            {
              "success": false,
              "status": 400,
              "message": "잘못된 요청입니다.",
              "data": null
            }
            """;

    private static final String UNAUTHORIZED_401_EXAMPLE = """
            {
              "success": false,
              "status": 401,
              "message": "인증이 필요합니다.",
              "data": null
            }
            """;

    private static final String FORBIDDEN_403_EXAMPLE = """
            {
              "success": false,
              "status": 403,
              "message": "권한이 없습니다.",
              "data": null
            }
            """;

    private static final String NOT_FOUND_404_EXAMPLE = """
            {
              "success": false,
              "status": 404,
              "message": "공지사항을 찾을 수 없습니다.",
              "data": null
            }
            """;

    private final NoticeService noticeService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(summary = "공지사항 목록 조회", description = "공지사항 목록을 조회합니다. title 값이 있으면 제목으로 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOTICE_LIST_SUCCESS_200_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE)))
    })
    @GetMapping
    public ResponseEntity<CommonResponse<NoticeListResponse>> getNoticeList(
            @Parameter(description = "검색할 공지사항 제목", example = "회의")
            @RequestParam(required = false) String title,
            @Parameter(description = "페이지 정보")
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(CommonResponse.success(noticeService.getNoticeList(title, pageable)));
    }

    @Operation(summary = "공지사항 상세 조회", description = "특정 공지사항의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOTICE_DETAIL_SUCCESS_200_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "공지사항 없음", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOT_FOUND_404_EXAMPLE)))
    })
    @GetMapping("/{noticeId}")
    public ResponseEntity<CommonResponse<NoticeDetailResponse>> getNoticeDetail(
            @Parameter(description = "공지사항 ID", example = "1")
            @PathVariable Long noticeId
    ) {
        return ResponseEntity.ok(CommonResponse.success(noticeService.getNoticeDetail(noticeId)));
    }

    @Operation(
            summary = "공지사항 작성",
            description = "공지사항을 작성합니다. ADMIN만 작성할 수 있으며, 첨부파일은 선택입니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = NoticeCreateMultipartRequest.class),
                            encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOTICE_CREATE_SUCCESS_201_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = BAD_REQUEST_400_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = FORBIDDEN_403_EXAMPLE)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<NoticeCreateResponse>> createNotice(
            @RequestPart("request") String requestJson,
            @Parameter(description = "첨부파일 목록")
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication authentication
    ) {
        NoticeCreateRequest request = parseRequest(requestJson);
        NoticeCreateResponse response = noticeService.createNotice(request, files, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(201, "공지사항이 작성되었습니다.", response));
    }

    @Operation(
            summary = "공지사항 수정",
            description = """
                    공지사항을 수정합니다. ADMIN만 수정할 수 있으며, 첨부파일은 선택입니다.
                    기존 파일 중 삭제할 파일은 request.deleteFileIds에 담습니다.
                    files가 있으면 기존 파일을 유지한 상태에서 새 파일이 추가됩니다.
                    """,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = NoticeUpdateMultipartRequest.class),
                            encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOTICE_UPDATE_SUCCESS_200_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = BAD_REQUEST_400_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = FORBIDDEN_403_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "공지사항 없음", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOT_FOUND_404_EXAMPLE)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{noticeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<NoticeUpdateResponse>> updateNotice(
            @Parameter(description = "공지사항 ID", example = "1")
            @PathVariable Long noticeId,
            @RequestPart("request") String requestJson,
            @Parameter(description = "첨부파일 목록")
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        NoticeUpdateRequest request = parseUpdateRequest(requestJson);
        return ResponseEntity.ok(CommonResponse.success(
                200,
                "공지사항이 수정되었습니다.",
                noticeService.updateNotice(noticeId, request, files)
        ));
    }

    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다. ADMIN만 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOTICE_DELETE_SUCCESS_200_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = FORBIDDEN_403_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "공지사항 없음", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOT_FOUND_404_EXAMPLE)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<CommonResponse<NoticeDeleteResponse>> deleteNotice(
            @Parameter(description = "공지사항 ID", example = "1")
            @PathVariable Long noticeId
    ) {
        return ResponseEntity.ok(CommonResponse.success(noticeService.deleteNotice(noticeId)));
    }

    private NoticeCreateRequest parseRequest(String requestJson) {
        try {
            NoticeCreateRequest request = objectMapper.readValue(requestJson, NoticeCreateRequest.class);
            return validateRequest(request);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "request JSON 형식이 올바르지 않습니다."
            );
        }
    }

    private NoticeUpdateRequest parseUpdateRequest(String requestJson) {
        try {
            NoticeUpdateRequest request = objectMapper.readValue(requestJson, NoticeUpdateRequest.class);
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return request;
    }
}
