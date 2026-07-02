package com.dcom.intranet.archive.repository;

import com.dcom.intranet.archive.domain.ArchiveFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveFileRepository extends JpaRepository<ArchiveFile, Long> {
}