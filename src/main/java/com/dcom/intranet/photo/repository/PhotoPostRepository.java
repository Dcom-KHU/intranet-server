package com.dcom.intranet.photo.repository;

import com.dcom.intranet.photo.domain.PhotoPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhotoPostRepository extends JpaRepository<PhotoPost, Long> {

    Page<PhotoPost> findByEventNameContaining(String eventName, Pageable pageable);

    boolean existsByAuthorId(Long authorId);

    @Query("""
            SELECT p
            FROM PhotoPost p
            WHERE REPLACE(p.eventName, ' ', '') LIKE CONCAT('%', :keyword, '%')
            """)
    Page<PhotoPost> searchByEventNameIgnoringSpaces(
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
