package com.dcom.intranet.info.controller;

import com.dcom.intranet.global.response.CommonResponse;
import com.dcom.intranet.global.swagger.SwaggerExamples;
import com.dcom.intranet.info.dto.request.InfoCommentCreateRequest;
import com.dcom.intranet.info.dto.request.InfoCommentUpdateRequest;
import com.dcom.intranet.info.dto.response.InfoCommentListResponse;
import com.dcom.intranet.info.dto.response.InfoCommentResponse;
import com.dcom.intranet.info.service.InfoCommentService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "정보공유 게시판 댓글", description = "정보공유 게시판 댓글 API")
@RestController
@RequestMapping("/api/info-posts/{postId}/comments")
@RequiredArgsConstructor
public class InfoCommentController {

    private final InfoCommentService infoCommentService;

    @Operation(
            summary = "정보공유 댓글 목록 조회",
            description = "특정 정보공유 게시글에 연결된 댓글 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "댓글 목록 조회 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_COMMENT_LIST_SUCCESS_200)
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
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_NOT_FOUND_404)
                    )
            )
    })
    @GetMapping
    public ResponseEntity<CommonResponse<InfoCommentListResponse>> getComments(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId
    ) {
        InfoCommentListResponse response = infoCommentService.getComments(postId);

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @Operation(
            summary = "정보공유 댓글 작성",
            description = "특정 정보공유 게시글에 댓글을 작성합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "댓글 작성 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_COMMENT_CREATE_SUCCESS_201)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_COMMENT_BAD_REQUEST_400)
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
                    description = "게시글 또는 사용자 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_POST_NOT_FOUND_404)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<CommonResponse<InfoCommentResponse>> createComment(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,

            @Valid @RequestBody InfoCommentCreateRequest request,
            Authentication authentication
    ) {
        InfoCommentResponse response =
                infoCommentService.createComment(postId, request, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(
                        201,
                        "댓글이 작성되었습니다.",
                        response
                ));
    }

    @Operation(
            summary = "정보공유 댓글 수정",
            description = "댓글을 수정합니다. 댓글 작성자만 수정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "댓글 수정 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_COMMENT_UPDATE_SUCCESS_200)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_COMMENT_BAD_REQUEST_400)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "수정 권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_COMMENT_UPDATE_FORBIDDEN_403)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글, 댓글 또는 사용자 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_COMMENT_NOT_FOUND_404)
                    )
            )
    })
    @PutMapping("/{commentId}")
    public ResponseEntity<CommonResponse<InfoCommentResponse>> updateComment(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,

            @Parameter(description = "댓글 ID", example = "1")
            @PathVariable Long commentId,

            @Valid @RequestBody InfoCommentUpdateRequest request,
            Authentication authentication
    ) {
        InfoCommentResponse response =
                infoCommentService.updateComment(postId, commentId, request, authentication.getName());

        return ResponseEntity.ok(
                CommonResponse.success(
                        200,
                        "댓글이 수정되었습니다.",
                        response
                )
        );
    }

    @Operation(
            summary = "정보공유 댓글 삭제",
            description = "댓글을 삭제합니다. 댓글 작성자 또는 ADMIN만 삭제할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "댓글 삭제 성공",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_COMMENT_DELETE_SUCCESS_200)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "삭제 권한 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_COMMENT_DELETE_FORBIDDEN_403)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "게시글, 댓글 또는 사용자 없음",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(value = SwaggerExamples.INFO_COMMENT_NOT_FOUND_404)
                    )
            )
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommonResponse<Void>> deleteComment(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,

            @Parameter(description = "댓글 ID", example = "1")
            @PathVariable Long commentId,

            Authentication authentication
    ) {
        infoCommentService.deleteComment(postId, commentId, authentication.getName());

        return ResponseEntity.ok(
                CommonResponse.success(
                        200,
                        "댓글이 삭제되었습니다."
                )
        );
    }
}
