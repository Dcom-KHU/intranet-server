package com.dcom.intranet.photo.controller;

import com.dcom.intranet.global.response.CommonResponse;
import com.dcom.intranet.photo.dto.PhotoCommentCreateRequest;
import com.dcom.intranet.photo.dto.PhotoCommentCreateResponse;
import com.dcom.intranet.photo.dto.PhotoCommentDeleteResponse;
import com.dcom.intranet.photo.dto.PhotoCommentUpdateResponse;
import com.dcom.intranet.photo.dto.PhotoPostCreateResponse;
import com.dcom.intranet.photo.dto.PhotoPostDeleteResponse;
import com.dcom.intranet.photo.dto.PhotoPostDetailResponse;
import com.dcom.intranet.photo.dto.PhotoPostListResponse;
import com.dcom.intranet.photo.service.PhotoPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Tag(name = "사진첩", description = "사진첩 API")
@RestController
@RequestMapping("/api/photo-posts")
@RequiredArgsConstructor
public class PhotoPostController {

    private static final String PHOTO_LIST_SUCCESS_200_EXAMPLE = """
            {
              "success": true,
              "status": 200,
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": {
                "albumList": [
                  {
                    "albumId": 1,
                    "coverImageUrl": "/uploads/photo/2026/07/cover.jpg",
                    "eventName": "신입생 환영회",
                    "activityDate": "2026-07-03"
                  }
                ],
                "pageInfo": {
                  "page": 0,
                  "size": 8,
                  "totalElements": 1,
                  "totalPages": 1,
                  "first": true,
                  "last": true
                }
              }
            }
            """;

    private static final String PHOTO_DETAIL_SUCCESS_200_EXAMPLE = """
            {
              "success": true,
              "status": 200,
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": {
                "albumId": 1,
                "eventName": "신입생 환영회",
                "activityDate": "2026-07-03",
                "imageList": [
                  "/uploads/photo/2026/07/photo1.jpg",
                  "/uploads/photo/2026/07/photo2.jpg"
                ],
                "description": "신입생 환영회 사진입니다.",
                "comments": [
                  {
                    "commentId": 1,
                    "authorId": 2,
                    "authorName": "홍길동",
                    "content": "좋은 사진 감사합니다.",
                    "createdAt": "2026-07-03T12:00:00",
                    "updatedAt": null
                  }
                ]
              }
            }
            """;

    private static final String PHOTO_SAVE_SUCCESS_EXAMPLE = """
            {
              "success": true,
              "status": 201,
              "message": "사진첩이 등록되었습니다.",
              "data": {
                "albumId": 1,
                "eventName": "신입생 환영회",
                "activityDate": "2026-07-03",
                "coverImageUrl": "/uploads/photo/2026/07/cover.jpg",
                "imageUrls": [
                  "/uploads/photo/2026/07/photo1.jpg",
                  "/uploads/photo/2026/07/photo2.jpg"
                ]
              }
            }
            """;

    private static final String PHOTO_UPDATE_SUCCESS_200_EXAMPLE = """
            {
              "success": true,
              "status": 200,
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": {
                "albumId": 1,
                "eventName": "신입생 환영회 수정",
                "activityDate": "2026-07-04",
                "coverImageUrl": "/uploads/photo/2026/07/cover-updated.jpg",
                "imageUrls": [
                  "/uploads/photo/2026/07/photo-updated1.jpg"
                ]
              }
            }
            """;

    private static final String PHOTO_DELETE_SUCCESS_200_EXAMPLE = """
            {
              "success": true,
              "status": 200,
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": {
                "message": "사진첩이 삭제되었습니다."
              }
            }
            """;

    private static final String COMMENT_CREATE_SUCCESS_201_EXAMPLE = """
            {
              "success": true,
              "status": 201,
              "message": "댓글이 작성되었습니다.",
              "data": {
                "commentId": 1,
                "content": "댓글 내용",
                "createdAt": "2026-07-03T12:00:00"
              }
            }
            """;

    private static final String COMMENT_UPDATE_SUCCESS_200_EXAMPLE = """
            {
              "success": true,
              "status": 200,
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": {
                "commentId": 1,
                "content": "수정된 댓글 내용",
                "updatedAt": "2026-07-03T13:00:00"
              }
            }
            """;

    private static final String COMMENT_DELETE_SUCCESS_200_EXAMPLE = """
            {
              "success": true,
              "status": 200,
              "message": "요청이 성공적으로 처리되었습니다.",
              "data": {
                "message": "댓글이 삭제되었습니다."
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
              "message": "요청한 리소스를 찾을 수 없습니다.",
              "data": null
            }
            """;

    private final PhotoPostService photoPostService;

    @Operation(summary = "사진첩 목록 조회", description = "사진첩 목록을 조회합니다. 대표 이미지는 첫 번째 업로드 사진이며, 기본 페이지 크기는 8개입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = PHOTO_LIST_SUCCESS_200_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE)))
    })
    @GetMapping
    public ResponseEntity<CommonResponse<PhotoPostListResponse>> getPhotoPostList(
            @Parameter(description = "페이지 정보. 기본 size는 8입니다.")
            @PageableDefault(size = 8, sort = "activityDate", direction = DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(CommonResponse.success(photoPostService.getPhotoPostList(pageable)));
    }

    @Operation(summary = "사진첩 상세 조회", description = "사진 목록, 설명, 활동 날짜, 댓글을 포함한 사진첩 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = PHOTO_DETAIL_SUCCESS_200_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "사진첩 없음", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOT_FOUND_404_EXAMPLE)))
    })
    @GetMapping("/{albumId}")
    public ResponseEntity<CommonResponse<PhotoPostDetailResponse>> getPhotoPostDetail(
            @Parameter(description = "사진첩 ID", example = "1")
            @PathVariable Long albumId
    ) {
        return ResponseEntity.ok(CommonResponse.success(photoPostService.getPhotoPostDetail(albumId)));
    }

    @Operation(summary = "사진첩 등록", description = "사진첩을 등록합니다. ADMIN만 등록할 수 있으며 요청 형식은 multipart/form-data입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = PHOTO_SAVE_SUCCESS_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = BAD_REQUEST_400_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = FORBIDDEN_403_EXAMPLE)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<PhotoPostCreateResponse>> createPhotoPost(
            @RequestPart("eventName") String eventName,
            @RequestPart("activityDate") String activityDate,
            @RequestPart("coverImage") MultipartFile coverImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "description", required = false) String description
    ) {
        PhotoPostCreateResponse response = photoPostService.createPhotoPost(
                eventName,
                parseActivityDate(activityDate),
                coverImage,
                images,
                description
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(201, "사진첩이 등록되었습니다.", response));
    }

    @Operation(summary = "사진첩 수정", description = "사진첩을 수정합니다. ADMIN만 수정할 수 있으며 요청 형식은 multipart/form-data입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = PHOTO_UPDATE_SUCCESS_200_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = BAD_REQUEST_400_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = FORBIDDEN_403_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "사진첩 없음", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOT_FOUND_404_EXAMPLE)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{albumId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<PhotoPostCreateResponse>> updatePhotoPost(
            @Parameter(description = "사진첩 ID", example = "1")
            @PathVariable Long albumId,
            @RequestPart("eventName") String eventName,
            @RequestPart("activityDate") String activityDate,
            @RequestPart("coverImage") MultipartFile coverImage,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "description", required = false) String description
    ) {
        PhotoPostCreateResponse response = photoPostService.updatePhotoPost(
                albumId,
                eventName,
                parseActivityDate(activityDate),
                coverImage,
                images,
                description
        );
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "사진첩 삭제", description = "사진첩을 삭제합니다. ADMIN만 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = PHOTO_DELETE_SUCCESS_200_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = FORBIDDEN_403_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "사진첩 없음", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOT_FOUND_404_EXAMPLE)))
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{albumId}")
    public ResponseEntity<CommonResponse<PhotoPostDeleteResponse>> deletePhotoPost(
            @Parameter(description = "사진첩 ID", example = "1")
            @PathVariable Long albumId
    ) {
        return ResponseEntity.ok(CommonResponse.success(photoPostService.deletePhotoPost(albumId)));
    }

    @Operation(summary = "댓글 작성", description = "사진첩에 댓글을 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = COMMENT_CREATE_SUCCESS_201_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = BAD_REQUEST_400_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "사진첩 없음", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOT_FOUND_404_EXAMPLE)))
    })
    @PostMapping("/{albumId}/comments")
    public ResponseEntity<CommonResponse<PhotoCommentCreateResponse>> createComment(
            @Parameter(description = "사진첩 ID", example = "1")
            @PathVariable Long albumId,
            @Valid @RequestBody PhotoCommentCreateRequest request,
            Authentication authentication
    ) {
        PhotoCommentCreateResponse response = photoPostService.createComment(
                albumId,
                request.content(),
                authentication.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(201, "댓글이 작성되었습니다.", response));
    }

    @Operation(summary = "댓글 수정", description = "사진첩 댓글을 수정합니다. 댓글 작성자만 수정할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = COMMENT_UPDATE_SUCCESS_200_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = BAD_REQUEST_400_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "댓글 작성자만 수정 가능", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = FORBIDDEN_403_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "사진첩 댓글 없음", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOT_FOUND_404_EXAMPLE)))
    })
    @PutMapping("/{albumId}/comments/{commentId}")
    public ResponseEntity<CommonResponse<PhotoCommentUpdateResponse>> updateComment(
            @Parameter(description = "사진첩 ID", example = "1")
            @PathVariable Long albumId,
            @Parameter(description = "댓글 ID", example = "1")
            @PathVariable Long commentId,
            @Valid @RequestBody PhotoCommentCreateRequest request,
            Authentication authentication
    ) {
        PhotoCommentUpdateResponse response = photoPostService.updateComment(
                albumId,
                commentId,
                request.content(),
                authentication.getName()
        );
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(summary = "댓글 삭제", description = "사진첩 댓글을 삭제합니다. 댓글 작성자 또는 ADMIN만 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = COMMENT_DELETE_SUCCESS_200_EXAMPLE))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = UNAUTHORIZED_401_EXAMPLE))),
            @ApiResponse(responseCode = "403", description = "댓글 작성자 또는 관리자만 삭제 가능", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = FORBIDDEN_403_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "사진첩 댓글 없음", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CommonResponse.class), examples = @ExampleObject(value = NOT_FOUND_404_EXAMPLE)))
    })
    @DeleteMapping("/{albumId}/comments/{commentId}")
    public ResponseEntity<CommonResponse<PhotoCommentDeleteResponse>> deleteComment(
            @Parameter(description = "사진첩 ID", example = "1")
            @PathVariable Long albumId,
            @Parameter(description = "댓글 ID", example = "1")
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        PhotoCommentDeleteResponse response = photoPostService.deleteComment(
                albumId,
                commentId,
                authentication.getName()
        );
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    private LocalDate parseActivityDate(String activityDate) {
        try {
            return LocalDate.parse(activityDate);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "activityDate는 yyyy-MM-dd 형식이어야 합니다."
            );
        }
    }
}
