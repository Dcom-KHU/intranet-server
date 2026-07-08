package com.dcom.intranet.info.repository;

import com.dcom.intranet.info.domain.InfoPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InfoPostRepository extends JpaRepository<InfoPost, Long> {

    Page<InfoPost> findByTitleContainingOrContentContaining(
            String titleKeyword,
            String contentKeyword,
            Pageable pageable
    );

    List<InfoPost> findByAuthorId(Long authorId);
}
