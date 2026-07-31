package com.dcom.intranet.archive.service;

import com.dcom.intranet.archive.repository.ArchiveFileRepository;
import com.dcom.intranet.archive.repository.ArchiveRecordRepository;
import com.dcom.intranet.archive.repository.ArchiveRepository;
import com.dcom.intranet.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArchiveServiceTest {

    private final ArchiveRepository archiveRepository = mock(ArchiveRepository.class);
    private final ArchiveService archiveService = new ArchiveService(
            archiveRepository,
            mock(UserRepository.class),
            mock(ArchiveFileRepository.class),
            mock(ArchiveFileStorageService.class),
            mock(ArchiveRecordRepository.class)
    );

    @Test
    @DisplayName("족보 검색은 검색어의 공백을 제거해 과목명과 교수명을 조회한다")
    void searchNormalizesWhitespace() {
        when(archiveRepository.searchBySubjectNameOrProfessorNameIgnoringSpaces(
                eq("자료구조"), any(Pageable.class)
        )).thenAnswer(invocation -> org.springframework.data.domain.Page.empty());

        archiveService.getArchives(0, 10, "자료 구조");

        verify(archiveRepository).searchBySubjectNameOrProfessorNameIgnoringSpaces(
                eq("자료구조"), any(Pageable.class)
        );
    }
}
