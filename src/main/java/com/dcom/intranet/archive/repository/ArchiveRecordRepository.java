package com.dcom.intranet.archive.repository;

import com.dcom.intranet.archive.domain.ArchiveRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveRecordRepository extends JpaRepository<ArchiveRecord, Long> {
}