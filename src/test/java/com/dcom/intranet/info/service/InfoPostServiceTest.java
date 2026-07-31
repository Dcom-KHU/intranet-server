package com.dcom.intranet.info.service;

import com.dcom.intranet.auth.repository.UserRepository;
import com.dcom.intranet.info.repository.InfoPostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InfoPostServiceTest {

    private final InfoPostRepository infoPostRepository = mock(InfoPostRepository.class);
    private final InfoPostService infoPostService = new InfoPostService(
            infoPostRepository,
            mock(UserRepository.class),
            mock(InfoPostFileStorageService.class)
    );

    @Test
    @DisplayName("정보공유 검색은 검색어의 공백을 제거해 제목과 본문을 조회한다")
    void searchNormalizesWhitespace() {
        when(infoPostRepository.searchByTitleOrContentIgnoringSpaces(
                eq("시간복잡도"), any(Pageable.class)
        )).thenAnswer(invocation -> org.springframework.data.domain.Page.empty());

        infoPostService.getPosts(0, 10, "시간 복잡도", "latest");

        verify(infoPostRepository).searchByTitleOrContentIgnoringSpaces(
                eq("시간복잡도"), any(Pageable.class)
        );
    }
}
