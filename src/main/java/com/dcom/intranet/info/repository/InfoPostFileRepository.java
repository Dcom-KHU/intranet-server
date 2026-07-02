package com.dcom.intranet.info.repository;

import com.dcom.intranet.info.domain.InfoPostFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoPostFileRepository extends JpaRepository<InfoPostFile, Long> {
}