package com.dcom.intranet.notice.service;

import com.dcom.intranet.notice.domain.Notice;
import com.dcom.intranet.notice.dto.NoticeCreateRequest;
import com.dcom.intranet.notice.dto.NoticeCreateResponse;
import com.dcom.intranet.notice.dto.NoticeDeleteResponse;
import com.dcom.intranet.notice.dto.NoticeDetailResponse;
import com.dcom.intranet.notice.dto.NoticeListResponse;
import com.dcom.intranet.notice.dto.NoticeUpdateResponse;
import com.dcom.intranet.notice.repository.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public NoticeService(NoticeRepository noticeRepository) {
        this.noticeRepository = noticeRepository;
    }

    @Transactional(readOnly = true)
    public NoticeListResponse getNoticeList(Pageable pageable) {
        Page<NoticeListResponse.NoticeSummary> page = noticeRepository.findAll(pageable)
                .map(notice -> new NoticeListResponse.NoticeSummary(
                        notice.getNoticeId(),
                        notice.getTitle(),
                        notice.getAuthorId(),
                        notice.getCreatedAt()
                ));

        return NoticeListResponse.from(page);
    }

    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeDetail(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return NoticeDetailResponse.from(notice);
    }

    @Transactional
    public NoticeCreateResponse createNotice(NoticeCreateRequest request) {
        Notice notice = new Notice(
                request.title(),
                request.content(),
                null,
                LocalDateTime.now(),
                request.toNoticeFiles()
        );

        Notice savedNotice = noticeRepository.save(notice);
        return NoticeCreateResponse.from(savedNotice);
    }

    @Transactional
    public NoticeUpdateResponse updateNotice(Long noticeId, NoticeCreateRequest request) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        notice.update(
                request.title(),
                request.content(),
                request.toNoticeFiles(),
                LocalDateTime.now()
        );

        return NoticeUpdateResponse.from(notice);
    }

    @Transactional
    public NoticeDeleteResponse deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        noticeRepository.delete(notice);
        return new NoticeDeleteResponse("Notice deleted successfully.");
    }
}
