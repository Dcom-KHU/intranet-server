package com.dcom.intranet.attachment.controller;

import com.dcom.intranet.attachment.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Operation(summary = "첨부파일 다운로드", description = "로그인한 회원이 정보공유 또는 공지사항 첨부파일을 다운로드합니다. type은 info-posts 또는 notice입니다.")
    @GetMapping("/{type}/{fileId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable String type,
            @PathVariable Long fileId
    ) {
        AttachmentService.DownloadFile file = attachmentService.download(type, fileId);
        String contentType = file.contentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : file.contentType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.fileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(file.resource());
    }
}
