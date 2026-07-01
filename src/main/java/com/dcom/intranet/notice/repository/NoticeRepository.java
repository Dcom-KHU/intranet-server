package com.dcom.intranet.notice.repository;

import com.dcom.intranet.notice.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findByTitleContaining(String title, Pageable pageable);
}
