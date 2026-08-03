package com.dcom.intranet.info.repository;

import com.dcom.intranet.info.domain.InfoPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InfoPostRepository extends JpaRepository<InfoPost, Long> {

    @Query("""
            SELECT p
            FROM InfoPost p
            WHERE REPLACE(p.title, ' ', '') LIKE CONCAT('%', :keyword, '%')
               OR REPLACE(p.content, ' ', '') LIKE CONCAT('%', :keyword, '%')
            """)
    Page<InfoPost> searchByTitleOrContentIgnoringSpaces(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    List<InfoPost> findByAuthorId(Long authorId);
}
