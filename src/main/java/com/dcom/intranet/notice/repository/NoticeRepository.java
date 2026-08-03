package com.dcom.intranet.notice.repository;

import com.dcom.intranet.notice.domain.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Page<Notice> findByTitleContaining(String title, Pageable pageable);

    @Query("""
            SELECT n
            FROM Notice n
            WHERE REPLACE(n.title, ' ', '') LIKE CONCAT('%', :keyword, '%')
            """)
    Page<Notice> searchByTitleIgnoringSpaces(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<Notice> findByAuthorId(Long authorId);
}
