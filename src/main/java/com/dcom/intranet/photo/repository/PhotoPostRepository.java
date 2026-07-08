package com.dcom.intranet.photo.repository;

import com.dcom.intranet.photo.domain.PhotoPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoPostRepository extends JpaRepository<PhotoPost, Long> {
}
