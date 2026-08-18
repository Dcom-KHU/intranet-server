package com.dcom.intranet.notice.repository;

import com.dcom.intranet.notice.domain.NoticeFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeFileRepository extends JpaRepository<NoticeFile, Long> {
}
