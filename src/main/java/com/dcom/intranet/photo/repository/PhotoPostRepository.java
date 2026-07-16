package com.dcom.intranet.photo.repository;

import com.dcom.intranet.photo.domain.PhotoPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoPostRepository extends JpaRepository<PhotoPost, Long> {

    Page<PhotoPost> findByEventNameContaining(String eventName, Pageable pageable);
}
