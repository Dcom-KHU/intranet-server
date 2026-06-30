package com.dcom.intranet.notice.controller;

import com.dcom.intranet.notice.dto.NoticeCreateRequest;
import com.dcom.intranet.notice.dto.NoticeCreateResponse;
import com.dcom.intranet.notice.dto.NoticeDeleteResponse;
import com.dcom.intranet.notice.dto.NoticeDetailResponse;
import com.dcom.intranet.notice.dto.NoticeListResponse;
import com.dcom.intranet.notice.dto.NoticeUpdateResponse;
import com.dcom.intranet.notice.service.NoticeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ResponseEntity<NoticeListResponse> getNoticeList(
            @PageableDefault(size = 10, sort = "createdAt", direction = DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(noticeService.getNoticeList(pageable));
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailResponse> getNoticeDetail(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.getNoticeDetail(noticeId));
    }

    @PostMapping
    public ResponseEntity<NoticeCreateResponse> createNotice(@Valid @RequestBody NoticeCreateRequest request) {
        return ResponseEntity.ok(noticeService.createNotice(request));
    }

    @PutMapping("/{noticeId}")
    public ResponseEntity<NoticeUpdateResponse> updateNotice(
            @PathVariable Long noticeId,
            @Valid @RequestBody NoticeCreateRequest request
    ) {
        return ResponseEntity.ok(noticeService.updateNotice(noticeId, request));
    }

    @DeleteMapping("/{noticeId}")
    public ResponseEntity<NoticeDeleteResponse> deleteNotice(@PathVariable Long noticeId) {
        return ResponseEntity.ok(noticeService.deleteNotice(noticeId));
    }
}
